package com.oneDev.ecommerce.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SearchProductsRequest {
    private String query;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String sortBy = "_score";
    private String sortOrder;
    private int page;
    private int size;
}
