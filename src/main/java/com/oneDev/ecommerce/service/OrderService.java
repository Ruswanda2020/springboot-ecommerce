package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.response.OrderItemResponse;
import com.oneDev.ecommerce.model.response.OrderResponse;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderResponse checkOut(CheckOutRequest checkOutRequest);
    Optional<Order> findOrderById(Long orderId);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
    void cancelOrder(Long orderId);
    List<OrderItemResponse> findOrderItemByOrderId(Long orderId);
    void updateOrderStatus(Long orderId, String status);
    Double calculateTotalPrice(Long orderId);

}
