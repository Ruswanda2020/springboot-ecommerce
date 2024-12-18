package com.oneDev.ecommerce.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.ecommerce.entity.UserAddresses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShippingRateRequest {

    private Address fromAddress;
    private Address toAddress;
    private BigDecimal totalWeightInGrams;

    @Data
    @Builder
    public static class Address {
        private String streetAddress;
        private String cityName;
        private String stateName;
        private String postalCode;
    }

    public static Address from(UserAddresses userAddresses) {
        return Address.builder()
                .cityName(userAddresses.getCity())
                .stateName(userAddresses.getState())
                .streetAddress(userAddresses.getStreetAddress())
                .postalCode(userAddresses.getPostalCode())
                .build();
    }
}
