package com.oneDev.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.oneDev.ecommerce.entity.Category;
import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.model.ProductDocument;
import com.oneDev.ecommerce.service.CategoryService;
import com.oneDev.ecommerce.service.ProductIdxService;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductIdxServiceImpl implements ProductIdxService {

    private final CategoryService categoryService;
    private final ElasticsearchClient elasticsearchClient;
    private final Retry elasticSearchIndexRetrier;
    private static final String INDEX_NAME = "products";

    @Override @Async
    public void reIndexProduct(Product product) {
        List<Category> categoryList = categoryService.getProductCategories(product.getProductId());
        ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categoryList);

        IndexRequest<ProductDocument> indexRequest = IndexRequest.of(builder ->
                builder.index(INDEX_NAME)
                        .id(String.valueOf(product.getProductId()))
                        .document(productDocument));


        try {
            elasticSearchIndexRetrier.executeCallable(() -> {
                        elasticsearchClient.index(indexRequest);
                        return null;
                    });

        } catch (IOException ex) {
            log.error("Error while reindex product with id: {}Error{}", product.getProductId(), ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override @Async
    public void deleteProduct(Product product) {
        DeleteRequest deleteRequest = DeleteRequest.of(builder ->
                builder.index(indexName())
                        .id(String.valueOf(product.getProductId())));
        try {
            elasticSearchIndexRetrier.executeCallable(() -> {
                elasticsearchClient.delete(deleteRequest);
                return null;
            });

        } catch (IOException ex) {
            log.error("Error while deleting product with id: {}Error{}", product.getProductId(), ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String indexName() {
        return INDEX_NAME;
    }
}
