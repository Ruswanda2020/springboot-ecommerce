package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.entity.Order;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.enumaration.OrderStatus;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.CheckOutRequest;
import com.oneDev.ecommerce.model.response.OrderItemResponse;
import com.oneDev.ecommerce.model.response.OrderResponse;
import com.oneDev.ecommerce.model.response.PaginatedOrderResponse;
import com.oneDev.ecommerce.service.OrderService;
import com.oneDev.ecommerce.utils.PageUtil;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<PaginatedOrderResponse> findOrdersByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "order_id, asc") String[] sort,
            @RequestParam(required = false) String name
    ) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        List<Sort.Order> sortOrder = PageUtil.parsSortOrderRequest(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder));

        Page<OrderResponse> orderResponses = orderService.findByUserIdAndPageable(
                userInfo.getUser().getUserId(), pageable);

        PaginatedOrderResponse paginatedOrderResponse = orderService.convertToOrderPage(orderResponses);
        return ResponseEntity.ok(paginatedOrderResponse);
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

               OrderStatus status;
        try {
           status =  OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Unrecognized order status: " + newStatus);
        }
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Double> calculateTotal(@PathVariable("orderId") Long orderId) {
        double total = orderService.calculateTotalPrice(orderId);
        return ResponseEntity.ok(total);
    }
}
