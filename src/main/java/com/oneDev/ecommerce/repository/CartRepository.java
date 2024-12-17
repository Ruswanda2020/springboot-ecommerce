package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    boolean existsByUserId(Long userId);
    Optional<Cart> findByUserId(Long userId);
}
