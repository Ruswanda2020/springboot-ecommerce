package com.oneDev.ecommerce.controller.admin;

import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.response.OrderResponse;
import com.oneDev.ecommerce.model.response.PaginatedOrderResponse;
import com.oneDev.ecommerce.service.OrderService;
import com.oneDev.ecommerce.utils.PageUtil;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/admin/orders")
@SecurityRequirement(name = "Bearer")
public class AdminOrderController {

    private final OrderService orderService;
    private final UserInfoHelper userInfoHelper;

    @GetMapping()
    public ResponseEntity<PaginatedOrderResponse> findOrdersByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "order_id,desc") String[] sort
    ) {

        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        List<Sort.Order> sortOrder = PageUtil.parsSortOrderRequest(sort);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder));

        Page<OrderResponse> userOrders = orderService.findByUserIdAndPageable(userInfo.getUser()
                .getUserId(), pageable);

        PaginatedOrderResponse paginatedOrderResponse = orderService.convertToOrderPage(userOrders);
        return ResponseEntity.ok(paginatedOrderResponse);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(OrderResponse.builder().build());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(@PathVariable("orderId") Long orderId) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        return orderService.findOrderById(orderId)
                .map(order -> {
                    OrderResponse orderResponse = OrderResponse.from(order);
                    return ResponseEntity.ok(orderResponse);
                }).orElse(ResponseEntity.notFound().build());
    }
}
