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
    private static final String INDEX_NAME = "products";

    @Override @Async
    public void reIndexProduct(Product product) {
        List<Category> categoryList = categoryService.getProductCategories(product.getProductId());
        ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categoryList);

        IndexRequest<ProductDocument> indexRequest = IndexRequest.of(builder ->
                builder.index(INDEX_NAME)
                        .id(String.valueOf(product.getProductId()))
                        .document(productDocument));

        IndexResponse response = null;
        try {
            response = elasticsearchClient.index(indexRequest);
        } catch (IOException ex) {
            log.error("Error while reindex product with id: {}Error{}", product.getProductId(), ex.getMessage());
        }

    }

    @Override
    public void deleteProduct(Product product) {
        DeleteRequest deleteRequest = DeleteRequest.of(builder ->
                builder.index(indexName())
                        .id(String.valueOf(product.getProductId())));

        DeleteResponse response = null;
        try {
            response = elasticsearchClient.delete(deleteRequest);
        } catch (IOException ex) {
            log.error("Error while deleting product with id: {}Error{}", product.getProductId(), ex.getMessage());
        }
    }

    @Override
    public String indexName() {
        return INDEX_NAME;
    }
}
