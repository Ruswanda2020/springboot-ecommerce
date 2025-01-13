package com.oneDev.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.oneDev.ecommerce.entity.Category;
import com.oneDev.ecommerce.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDocument {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal weight;
    private Integer stockQuantity;
    private Long userId;
    private Long purchaseCount;
    private Long viewCount;
    private List<CategoryInfo> categories;
    private String nameSuggest;
    private String nameNgram;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {

        private Long categoryId;
        private String name;
    }

    public static ProductDocument fromProductAndCategories(Product product,
                                                           List<Category> categories) {
        ProductDocument productDocument = ProductDocument.builder()
                .id(String.valueOf(product.getProductId()))
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .weight(product.getWeight())
                .stockQuantity(product.getStockQuantity())
                .userId(product.getUserId())
                .nameSuggest(product.getName())
                .nameNgram(product.getName())
                .build();
        List<CategoryInfo> categoryInfos = categories.stream().map(category -> {
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setCategoryId(category.getCategoryId());
            categoryInfo.setName(category.getName());
            return categoryInfo;
        }).toList();
        productDocument.setCategories(categoryInfos);
        return productDocument;
    }
}
