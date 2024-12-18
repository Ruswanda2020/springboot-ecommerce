package com.oneDev.ecommerce.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShippingOrderRequest {

    private Long orderId;
    private Address fromAddress;
    private Address toAddress;
    private int totalWeightInGrams;

    @Data
    @Builder
    public static class Address {
        private String streetAddress;
        private String cityName;
        private String stateName;
        private String postalCode;
    }
}
