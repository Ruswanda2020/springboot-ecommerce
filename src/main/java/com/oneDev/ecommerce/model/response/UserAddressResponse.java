package com.oneDev.ecommerce.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.ecommerce.entity.UserAddresses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserAddressResponse implements Serializable {

    private Long userAddressId;
    private String addressName;
    private String streetName;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public static UserAddressResponse from (UserAddresses userAddresses){
        return UserAddressResponse.builder()
                .userAddressId(userAddresses.getUserAddressId())
                .addressName(userAddresses.getAddressName())
                .streetName(userAddresses.getState())
                .city(userAddresses.getCity())
                .state(userAddresses.getState())
                .postalCode(userAddresses.getPostalCode())
                .country(userAddresses.getCountry())
                .build();
    }
}
