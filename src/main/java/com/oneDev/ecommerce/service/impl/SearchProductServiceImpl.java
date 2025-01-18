package com.oneDev.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.oneDev.ecommerce.model.ProductDocument;
import com.oneDev.ecommerce.model.request.SearchProductsRequest;
import com.oneDev.ecommerce.model.response.ProductResponse;
import com.oneDev.ecommerce.model.response.SearchProductsResponse;
import com.oneDev.ecommerce.service.ProductIdxService;
import com.oneDev.ecommerce.service.ProductService;
import com.oneDev.ecommerce.service.SearchProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchProductServiceImpl implements SearchProductService {

    private final ElasticsearchClient elasticsearchClient;
    private final ProductIdxService productIdxService;
    private final ProductService productService;

    @Override
    public SearchProductsResponse<ProductResponse> search(SearchProductsRequest searchRequest) {
        log.debug("Search request received: {}", searchRequest); // Log permintaan yang diterima

        BoolQuery.Builder boolQBuilder = new BoolQuery.Builder();

        // Full text search pada field `name` dan `description`
        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            boolQBuilder.must(MultiMatchQuery.of(mm ->
                            mm.fields("name", "description")
                                    .query(searchRequest.getQuery()))
                    ._toQuery());
            log.debug("Full text search applied: {}", searchRequest.getQuery()); // Log query full-text
        }

        // Filter berdasarkan kategori
        if (searchRequest.getCategory() != null && !searchRequest.getCategory().isEmpty()) {
            Query nestedQuery = NestedQuery.of(
                    nested -> nested
                            .path("categories")
                            .query(query -> query
                                    .term(term -> term
                                            .field("categories.name.keyword")
                                            .value(searchRequest.getCategory())
                                    )
                            )
            )._toQuery();
            boolQBuilder.filter(nestedQuery);
            log.debug("Category filter applied: {}", searchRequest.getQuery()); // Log filter kategori
        }

        // Filter berdasarkan rentang harga
        if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null) {
            RangeQuery.Builder rangeQBuilder = new RangeQuery.Builder().field("price");

            if (searchRequest.getMinPrice() != null) {
                rangeQBuilder.gte(JsonData.of(searchRequest.getMinPrice()));
                log.debug("Min price filter applied: {}", searchRequest.getMinPrice());
            }

            if (searchRequest.getMaxPrice() != null) {
                rangeQBuilder.lte(JsonData.of(searchRequest.getMaxPrice()));
                log.debug("Max price filter applied: {}", searchRequest.getMaxPrice());
            }

            boolQBuilder.filter(rangeQBuilder.build()._toQuery());
        }

        // Membangun query pencarian utama
        SearchRequest.Builder search = new SearchRequest.Builder()
                .index(productIdxService.indexName())
                .query(boolQBuilder.build()._toQuery());

        // Menambahkan pengurutan (sorting)
        search.sort(s ->
                s.field(f ->
                        f.field(searchRequest.getSortBy())
                                .order(
                                        "asc".equals(searchRequest.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc)));
        log.debug("Sorting applied: {} {}", searchRequest.getSortBy(), searchRequest.getSortOrder());

        // Menambahkan pagination
        search.from((searchRequest.getPage() - 1) * searchRequest.getSize())
                .size(searchRequest.getSize());
        log.debug("Pagination applied: page {} size {}", searchRequest.getPage(), searchRequest.getSize());

        // Menambahkan agregasi kategori
        search.aggregations("categories", a ->
                        a.nested(n -> n.path("categories"))
                                .aggregations("category_name", sa ->
                                        sa.terms(t ->
                                                t.field("categories.name.keyword"))))
                .from(searchRequest.getPage());

        // Menyusun request Elasticsearch
        SearchRequest elasticRequest = search.build();
        log.debug("Query being sent to Elasticsearch: {}", elasticRequest.toString());

        SearchProductsResponse<ProductResponse> response = new SearchProductsResponse<>();
        try {
            // Melakukan pencarian di Elasticsearch
            SearchResponse<ProductDocument> result = elasticsearchClient.search(elasticRequest, ProductDocument.class);
            log.debug("Elasticsearch search results: {}", result.hits().total().value()); // Log hasil pencarian

            // Mengonversi hasil hits ke dalam `ProductResponse`
            List<ProductResponse> productResponses = result.hits().hits()
                    .stream()
                    .filter(productDocumentHit -> productDocumentHit != null && productDocumentHit.id() != null)
                    .map(productDocumentHit -> Long.parseLong(productDocumentHit.id()))
                    .map(productService::findProductById)
                    .toList();

            response.setData(productResponses);
            log.debug("Processed {} products.", productResponses.size()); // Log jumlah produk yang diproses

            // Mendapatkan total hits
            if (result.hits().total() != null) {
                response.setTotalHits(result.hits().total().value());
            }

            // Mendapatkan agregasi (facet) kategori
            if (result.aggregations() != null) {
                Map<String, List<SearchProductsResponse.FacetEntry>> facets = new HashMap<>();
                var categoriesAgg = result.aggregations().get("categories");
                if (categoriesAgg != null && categoriesAgg.nested() != null) {
                    var categoryNameAgg = categoriesAgg.nested().aggregations().get("category_name");
                    if (categoryNameAgg != null && categoryNameAgg.sterms() != null) {
                        List<SearchProductsResponse.FacetEntry> categoryFacets = categoryNameAgg.sterms().buckets().array()
                                .stream()
                                .map(bucket -> new SearchProductsResponse.FacetEntry(bucket.key().stringValue(), bucket.docCount()))
                                .toList();
                        facets.put("categories", categoryFacets);
                    }
                }
                response.setFacets(facets);
                log.debug("Aggregations: {}", facets); // Log hasil agregasi kategori
            }
        } catch (IOException e) {
            // Menangani error saat melakukan pencarian
            log.error("Error while performing search. error message: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        // Mengembalikan hasil pencarian
        log.debug("Returning search response with {} products.", response.getData().size());
        return response;
    }
}
