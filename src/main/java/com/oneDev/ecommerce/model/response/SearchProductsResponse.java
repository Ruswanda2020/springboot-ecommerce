package com.oneDev.ecommerce.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SearchProductsResponse<T> {

    private List<T> data;
    private long totalHits;
    private Map<String, List<FacetEntry>> facets;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FacetEntry {

        private String key;
        private Long docCount;
    }
}
