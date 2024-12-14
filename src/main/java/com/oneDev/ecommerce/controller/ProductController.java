package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.model.PaginatedProductResponse;
import com.oneDev.ecommerce.model.ProductRequest;
import com.oneDev.ecommerce.model.ProductResponse;
import com.oneDev.ecommerce.service.ProductService;
import com.oneDev.ecommerce.utils.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable Long productId) {
    ProductResponse productResponse = productService.findProductById(productId);
    return ResponseEntity.ok(productResponse);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.createNewProduct(productRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productResponse);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long productId,
                                                         @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.updateProductById(productId, productRequest);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<PaginatedProductResponse> findAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "product_id, asc") String[] sort,
            @RequestParam(required = false) String name
    ) {

        List<Sort.Order> orders = PageUtil.parsSortOrderRequest(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<ProductResponse> productResponses;
        if (name != null && !name.isEmpty()) {
            productResponses =productService.findByNameAndPageable(name, pageable);
        } else {
            productResponses = productService.findByPage(pageable);
        }
        return ResponseEntity.ok(productService.convertProductPage(productResponses));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();
    }
}
