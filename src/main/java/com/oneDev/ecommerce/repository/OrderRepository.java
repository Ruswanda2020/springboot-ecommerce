package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);


    @Query(value = """
    SELECT * FROM orders
    WHERE user_id = :userId
    AND order_date BETWEEN :from AND :to
""", nativeQuery = true)
    List<Order> findByUserIdAndDateRange(@Param("userId") Long userId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime  to);

    Optional<Order> findByXenditInvoiceId(String xenditInvoiceId);
    List<Order> findByStatusAndOrderDateBefore(OrderStatus orderStatus, LocalDateTime dateTime);

}
