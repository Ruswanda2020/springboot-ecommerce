package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = """
    SELECT * FROM category
    WHERE lower("name") like :name
""", nativeQuery = true)
    List<Category> findByName(String name);
}