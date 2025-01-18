package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.SearchProductsRequest;
import com.oneDev.ecommerce.model.response.PaginatedProductResponse;
import com.oneDev.ecommerce.model.request.ProductRequest;
import com.oneDev.ecommerce.model.response.ProductResponse;
import com.oneDev.ecommerce.model.response.SearchProductsResponse;
import com.oneDev.ecommerce.service.ProductService;
import com.oneDev.ecommerce.service.SearchProductService;
import com.oneDev.ecommerce.utils.PageUtil;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final UserInfoHelper userInfoHelper;
    private final SearchProductService searchProductService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable Long productId) {
    ProductResponse productResponse = productService.findProductById(productId);
    return ResponseEntity.ok(productResponse);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        productRequest.setUser(userInfo.getUser());
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

    @PostMapping("/search")
    public ResponseEntity<SearchProductsResponse<ProductResponse>> search(@RequestBody SearchProductsRequest searchProductsRequest){
        log.info("Incoming search request: {}", searchProductsRequest);
        SearchProductsResponse<ProductResponse> response = searchProductService.search(searchProductsRequest);
        return ResponseEntity.ok(response);
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
