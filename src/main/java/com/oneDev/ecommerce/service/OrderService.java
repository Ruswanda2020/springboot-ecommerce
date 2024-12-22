package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.response.OrderItemResponse;
import com.oneDev.ecommerce.model.response.OrderResponse;
import com.oneDev.ecommerce.model.response.PaginatedOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderResponse checkOut(CheckOutRequest checkOutRequest);
    Optional<Order> findOrderById(Long orderId);
    List<Order> findByUserId(Long userId);
    Page<OrderResponse> findByUserIdAndPageable(Long userId, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    void cancelOrder(Long orderId);
    List<OrderItemResponse> findOrderItemByOrderId(Long orderId);
    void updateOrderStatus(Long orderId, OrderStatus status);
    Double calculateTotalPrice(Long orderId);
    PaginatedOrderResponse convertToOrderPage(Page<OrderResponse> orderResponses);


}
