package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Category;
import com.oneDev.ecommerce.repository.CategoryRepository;
import com.oneDev.ecommerce.repository.ProductCategoryRepository;
import com.oneDev.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public List<Category> findAll(List<Long> categoryIds) {
        return categoryIds.stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId)))
                .toList();
    }

    @Override
    public List<Category> getProductCategories(Long productId) {
        List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(productId)
                .stream()
                .map(productCategory -> productCategory.getId().getCategoryId())
                .toList();
        return categoryRepository.findAllById(categoryIds);
    }
}
