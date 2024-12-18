package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    boolean existsByUserId(Long userId);
    Optional<Cart> findByUserId(Long userId);
}
