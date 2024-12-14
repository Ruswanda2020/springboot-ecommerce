package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findAll(List<Long> categoryIds);

}
