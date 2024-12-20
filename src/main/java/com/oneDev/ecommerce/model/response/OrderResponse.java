package com.oneDev.ecommerce.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.ecommerce.entity.Order;
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
public class OrderResponse implements Serializable {
    private Long orderId;
    private Long productId;
    private BigDecimal subtotal;
    private BigDecimal taxFee;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDateTime orderDate;
    private String xenditInvoiceId;
    private String xenditPaymentStatus;
    private String xenditPaymentMethod;
    private String xenditPaymentUrl;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .productId(order.getOrderId())
                .subtotal(order.getSubtotal())
                .taxFee(order.getTaxFee())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .xenditInvoiceId(order.getXenditInvoiceId())
                .xenditPaymentStatus(order.getXenditPaymentStatus())
                .xenditPaymentMethod(order.getXenditPaymentMethod())
                .build();
    }
}
