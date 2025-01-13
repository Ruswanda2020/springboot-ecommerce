package com.oneDev.ecommerce.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentNotification implements Serializable {

    private String id;
    private BigDecimal amount;
    private String status;
    private Instant created;
    @JsonProperty(value = "is_high")
    private boolean isHigh;
    private Instant paidAt;
    private Instant updated;
    private String userId;
    private String currency;
    private String paymentId;
    private String description;
    private String externalId;
    private BigDecimal paidAmount;
    private String payerEmail;
    private String walletType;
    private String merchantName;
    private String paymentMethod;
    private String paymentChannel;
    private String paymentMethodId;

}
