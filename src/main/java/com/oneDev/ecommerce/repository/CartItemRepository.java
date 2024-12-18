package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query(value = """
    SELECT ci.* FROM cart_items ci
    JOIN carts c ON c.cart_id = ci.cart_id
    WHERE c.user_id = :userId
""", nativeQuery = true)
    List<CartItem> getUserCartItems(@Param("userId") Long userId);

    @Query(value = """
    SELECT ci.* FROM cart_items ci
    WHERE ci.cart_id = :cartId
    AND ci.product_id = :productId
""", nativeQuery = true)
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Modifying
    @Query(value = """
    DELETE FROM cart_items
    WHERE cart_id = :cartId
""", nativeQuery = true)
    void deleteAllByCartId(@Param("cartId")Long cartId);
}
