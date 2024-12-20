package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.response.OrderItemResponse;
import com.oneDev.ecommerce.model.response.OrderResponse;
import com.oneDev.ecommerce.service.OrderService;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@SecurityRequirement(name = "Bearer")
public class OrderController {

    private final OrderService orderService;
    private final UserInfoHelper userInfoHelper;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CheckOutRequest checkOutRequest) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        checkOutRequest.setUserId(userInfo.getUser().getUserId());
        OrderResponse orderResponse = orderService.checkOut(checkOutRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(@PathVariable("orderId") Long orderId) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        return orderService.findOrderById(orderId)
                .map(order -> {
                    if (!order.getUserId().equals(userInfo.getUser().getUserId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(OrderResponse.builder().build());
                    }
                    OrderResponse orderResponse = OrderResponse.from(order);
                    return ResponseEntity.ok(orderResponse);
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findOrdersByUserId() {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        List<Order> userOrders = orderService.findByUserId(userInfo.getUser().getUserId());
        List<OrderResponse> orderResponses = userOrders.stream()
                .map(OrderResponse::from).toList();
        return ResponseEntity.ok(orderResponses);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") Long orderId) {
         orderService.cancelOrder(orderId);
         return ResponseEntity.ok(OrderResponse.builder().build());
    }

    @GetMapping("/{orderId}/items")
    public  ResponseEntity<List<OrderItemResponse>> findOrderItemsByOrderId(@PathVariable("orderId") Long orderId) {
        List<OrderItemResponse> orderItemResponseList = orderService.findOrderItemByOrderId(orderId);
        return ResponseEntity.ok(orderItemResponseList);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable("orderId") Long orderId,
                                                           @RequestParam String newStatus) {
        orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Double> calculateTotal(@PathVariable("orderId") Long orderId) {
        double total = orderService.calculateTotalPrice(orderId);
        return ResponseEntity.ok(total);
    }
}
