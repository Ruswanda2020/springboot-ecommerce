package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Category;
import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.entity.ProductCategory;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.response.CategoryResponse;
import com.oneDev.ecommerce.model.response.PaginatedProductResponse;
import com.oneDev.ecommerce.model.request.ProductRequest;
import com.oneDev.ecommerce.model.response.ProductResponse;
import com.oneDev.ecommerce.repository.ProductCategoryRepository;
import com.oneDev.ecommerce.repository.ProductRepository;
import com.oneDev.ecommerce.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductCategoryRepository productCategoryRepository;
    private final String PRODUCT_CACHE_KEY = "products:";
    private final CacheService cacheService;
    private final RateLimitingService rateLimitingService;
    private final ProductIdxService productIdxService;


    @Override
    public Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable) {
        name = "%" + name + "%";
        name = name.toLowerCase();
        return productRepository.findByNamePageable(name, pageable)
                .map(product -> {
                    List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                    return ProductResponse.fromProductAndCategories(product, productCategories);
                });
    }

    @Override
    public ProductResponse findProductById(Long productId) {
        String cacheKey = PRODUCT_CACHE_KEY + productId;
        Optional<ProductResponse> cachedProduct = cacheService.get(cacheKey, ProductResponse.class);
        if (cachedProduct.isPresent()){
            return cachedProduct.get();
        }

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND));

        List<CategoryResponse> productCategories = getProductCategories(productId);
        ProductResponse productResponse = ProductResponse.fromProductAndCategories(existingProduct, productCategories);
        cacheService.put(cacheKey, productResponse);
        return productResponse;
    }

    @Override @Transactional
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        List<Category> categories = categoryService.findAll(productRequest.getCategoryIds());
        Product product = Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .weight(productRequest.getWeight())
                .userId(productRequest.getUser().getUserId())
                .StockQuantity(productRequest.getStockQuantity())
                .description(productRequest.getDescription())
                .build();

        Product savedProduct = productRepository.save(product);

        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = ProductCategory.builder().build();
                    ProductCategory.ProductCategoryId productCategoryId = new ProductCategory.ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(savedProduct.getProductId());
                    productCategory.setId(productCategoryId);
                    return productCategory;
                }).toList();

        productCategoryRepository.saveAll(productCategories);

        List<CategoryResponse> categoryResponseList = categories.stream()
                .map(CategoryResponse::from).toList();

        String cacheKey = PRODUCT_CACHE_KEY + savedProduct.getProductId();
        ProductResponse productResponse = ProductResponse.fromProductAndCategories(savedProduct, categoryResponseList);
        cacheService.put(cacheKey, productResponse);
        productIdxService.reIndexProduct(savedProduct);
        return productResponse;
    }

    @Override @Transactional
    public ProductResponse updateProductById(Long productId, ProductRequest productRequest) {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> {
                    return new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                            ExceptionType.RESOURCE_NOT_FOUND.getFormattedMessage("with product id=" + productId));
                });

        List<Category> categories = categoryService.findAll(productRequest.getCategoryIds());

        existingProduct.setName(productRequest.getName());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setWeight(productRequest.getWeight());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        existingProduct.setDescription(productRequest.getDescription());
        productRepository.save(existingProduct);

        List<ProductCategory> existingProductCategories = productCategoryRepository.findCategoriesByProductId(productId);
        productCategoryRepository.deleteAll(existingProductCategories);


        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = ProductCategory.builder().build();
                    ProductCategory.ProductCategoryId productCategoryId = new ProductCategory.ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(productId);
                    productCategory.setId(productCategoryId);
                    return productCategory;
                }).toList();

        productCategoryRepository.saveAll(productCategories);

        List<CategoryResponse> categoryResponseList = categories.stream()
                .map(CategoryResponse::from).toList();

        String cacheKey = PRODUCT_CACHE_KEY + productId;
        cacheService.evict(cacheKey);
        productIdxService.reIndexProduct(existingProduct);
        return ProductResponse.fromProductAndCategories(existingProduct, categoryResponseList);
    }

    @Override @Transactional
    public void deleteProductById(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        ExceptionType.RESOURCE_NOT_FOUND.getFormattedMessage("with product id=" + productId)));
        List<ProductCategory> existingProductCategories = productCategoryRepository.findCategoriesByProductId(productId);

        productCategoryRepository.deleteAll(existingProductCategories);
        productIdxService.deleteProduct(existingProduct);
        productRepository.delete(existingProduct);

    }

    @Override
    public PaginatedProductResponse convertProductPage(Page<ProductResponse> productPage) {
        return PaginatedProductResponse.builder()
                .data(productPage.getContent())
                .pageNo(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    public Page<ProductResponse> findByPage(Pageable pageable) {
        return rateLimitingService.executeWithRateLimit("product_listing",
                () -> productRepository.findByPageable(pageable)
                        .map(product -> {
                            List<CategoryResponse> categoryResponses = getProductCategories(product.getProductId());
                            return ProductResponse.fromProductAndCategories(product, categoryResponses);
                        }));
    }

    private List<CategoryResponse> getProductCategories(Long productId) {
        List<ProductCategory> productCategories = productCategoryRepository.findCategoriesByProductId(productId);
        List<Long> categoryIds = productCategories.stream()
                .map(productCategory -> productCategory.getId().getCategoryId()).toList();
        return categoryService.findAll(categoryIds)
                .stream().map(CategoryResponse::from).toList();
    }
}
