package com.oneDev.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.oneDev.ecommerce.entity.Category;
import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.model.ProductDocument;
import com.oneDev.ecommerce.repository.CategoryRepository;
import com.oneDev.ecommerce.repository.ProductCategoryRepository;
import com.oneDev.ecommerce.repository.ProductRepository;
import com.oneDev.ecommerce.service.BulkReindexService;
import com.oneDev.ecommerce.service.ProductIdxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkReindexServiceImpl implements BulkReindexService {

    // Repository untuk mengambil data produk dari database
    private final ProductRepository productRepository;

    // Klien Elasticsearch untuk melakukan operasi indeks
    private final ElasticsearchClient elasticsearchClient;

    // Repository untuk mengambil data relasi antara produk dan kategori
    private final ProductCategoryRepository productCategoryRepository;

    // Repository untuk mengambil data kategori dari database
    private final CategoryRepository categoryRepository;

    // Service untuk menangani nama indeks produk
    private final ProductIdxService productIdxService;

    // Ukuran batch untuk operasi bulk indexing
    private static final int BATCH_SIZE = 100;

    @Override
    @Async // Menandai metode ini agar berjalan secara asynchronous
    @Transactional(readOnly = true) // Menjalankan metode dalam mode transaksi hanya-baca
    public void reindexAllProducts() throws IOException {
        // Mencatat waktu awal proses reindexing
        long startTime = System.currentTimeMillis();

        // Counter untuk jumlah dokumen yang telah diindeks
        AtomicLong totalIndexed = new AtomicLong();

        // Batch dokumen untuk operasi bulk indexing
        List<ProductDocument> batch = new ArrayList<>(BATCH_SIZE);

        // Mengambil semua data produk dari database menggunakan stream
        try (Stream<Product> products = productRepository.streamAll()) {
            products.forEach(product -> {
                // Mendapatkan ID kategori yang terkait dengan produk
                List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(
                                product.getProductId())
                        .stream()
                        .map(productCategory -> productCategory.getId().getCategoryId())
                        .toList();

                // Mengambil data kategori berdasarkan ID
                List<Category> categories = categoryRepository.findAllById(categoryIds);

                // Membuat dokumen Elasticsearch dari data produk dan kategori
                ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categories);
                batch.add(productDocument); // Menambahkan dokumen ke batch

                // Jika ukuran batch sudah mencapai batas yang ditentukan, lakukan indexing
                if (batch.size() >= BATCH_SIZE) {
                    try {
                        // Melakukan indexing untuk batch yang sudah dikumpulkan
                        totalIndexed.addAndGet(indexBatch(batch));
                    } catch (IOException e) {
                        log.error("Error saat melakukan reindex: {}", e.getMessage());
                        throw new RuntimeException(e); // Melemparkan runtime exception jika terjadi error
                    }
                    batch.clear(); // Membersihkan batch setelah indexing selesai
                }
            });
        }

        // Jika masih ada dokumen di batch yang belum diindeks, lakukan indexing
        if (!batch.isEmpty()) {
            totalIndexed.addAndGet(indexBatch(batch));
        }

        // Mencatat waktu akhir proses reindexing
        long endTime = System.currentTimeMillis();

        // Mencetak log hasil proses reindexing
        log.info("Reindex selesai. Total dokumen yang diindeks: {}. Waktu yang dibutuhkan: {} ms", totalIndexed, (endTime - startTime));
    }

    // Metode untuk melakukan indexing pada satu batch dokumen
    private long indexBatch(List<ProductDocument> batch) throws IOException {
        // Membuat builder untuk operasi bulk request
        BulkRequest.Builder builder = new BulkRequest.Builder();

        // Menambahkan setiap dokumen dalam batch ke dalam builder bulk request
        for (ProductDocument document : batch) {
            builder.operations(op ->
                    op.update(upd ->
                            upd.index(productIdxService.indexName()) // Menentukan nama indeks
                                    .id(document.getId()) // Menentukan ID dokumen
                                    .action(act ->
                                            act.docAsUpsert(true) // Jika dokumen belum ada, buat dokumen baru
                                                    .doc(document)))); // Menambahkan dokumen ke indeks
        }

        // Mengirimkan permintaan bulk request ke Elasticsearch
        BulkResponse result = elasticsearchClient.bulk(builder.build());

        // Mengecek apakah ada error selama operasi bulk
        if (result.errors()) {
            log.error("Terjadi error saat melakukan operasi bulk");
            for (BulkResponseItem item : result.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason()); // Mencetak alasan error untuk setiap item
                }
            }
        }

        return batch.size(); // Mengembalikan jumlah dokumen dalam batch
    }
}
