package com.Onedev.ecommerce.controller;

import com.Onedev.ecommerce.model.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ProductResponse.builder()
                        .name("product-name"+ id)
                        .price(BigDecimal.ONE)
                        .description("description product")
                .build());
    }

        
}
