package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.request.SearchProductsRequest;
import com.oneDev.ecommerce.model.response.ProductResponse;
import com.oneDev.ecommerce.model.response.SearchProductsResponse;

public interface SearchProductService {

    SearchProductsResponse<ProductResponse> search(SearchProductsRequest request);
}
