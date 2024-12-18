package com.oneDev.ecommerce.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.ecommerce.entity.OrdersItems;
import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.entity.UserAddresses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItemResponse implements Serializable {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private UserAddressResponse shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderItemResponse from(OrdersItems ordersItems,
                                         UserAddresses userAddresses,
                                         Product product) {
        BigDecimal totalPrice = ordersItems.getPrice()
                .multiply(BigDecimal.valueOf(ordersItems.getQuantity()));

        return OrderItemResponse.builder()
                .orderItemId(ordersItems.getOrderItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .price(totalPrice)
                .quantity(ordersItems.getQuantity())
                .shippingAddress(UserAddressResponse.from(userAddresses))
                .createdAt(ordersItems.getCreatedAt())
                .updatedAt(ordersItems.getUpdatedAt())
                .build();
    }
}
