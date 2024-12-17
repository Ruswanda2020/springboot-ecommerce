package com.oneDev.ecommerce.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.ecommerce.entity.CartItem;
import com.oneDev.ecommerce.entity.Product;
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
public class CartItemResponse implements Serializable {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private BigDecimal weight;
    private BigDecimal totalPrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse from(CartItem cartItem,  Product product) {
        BigDecimal totalPrice = cartItem.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        BigDecimal totalWeight = product.getWeight()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productName(product.getName())
                .productId(product.getProductId())
                .price(cartItem.getPrice())
                .weight(totalWeight)
                .totalPrice(totalPrice)
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
