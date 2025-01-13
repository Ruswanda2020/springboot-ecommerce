package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.entity.Product;

public interface ProductIdxService {
    void reIndexProduct(Product product);
    void deleteProduct(Product product);
    String indexName();
}
