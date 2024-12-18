package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.OrdersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrdersItems, Long> {

    List<OrdersItems> findByOrderId(Long ordersId);
    @Query(value = """
        SELECT oi.* FROM orders_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE o.user_id = :userId
        AND oi.product_id = :productId
""", nativeQuery = true)
    List<OrdersItems> findByUserAndProduct(@Param("userId") Long userId,
                                           @Param("productId") Long productId);

    @Query(value = """
    SELECT o.total_amount + COALESCE(sum(oi.price * oi.quantity), 0) 
    FROM orders o
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.order_id = :orderId
    GROUP BY o.order_id
""", nativeQuery = true)
    Double calculateTotalOrder(@Param("orderId") Long orderId);

}
