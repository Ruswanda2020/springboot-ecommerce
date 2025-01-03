package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.response.PaginatedProductResponse;
import com.oneDev.ecommerce.model.request.ProductRequest;
import com.oneDev.ecommerce.model.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {


    ProductResponse findProductById(Long ProductId);
    ProductResponse createNewProduct(ProductRequest productRequest);
    ProductResponse updateProductById(Long productId, ProductRequest productRequest);
    void deleteProductById(Long productId);
    Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable);
    PaginatedProductResponse convertProductPage(Page<ProductResponse> productPage);
    Page<ProductResponse> findByPage(Pageable pageable);

}
