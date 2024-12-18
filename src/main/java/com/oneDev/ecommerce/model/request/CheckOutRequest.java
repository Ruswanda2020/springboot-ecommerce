package com.oneDev.ecommerce.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckOutRequest {

    private Long userId;

    @NotEmpty(message = "At least one cart item must be selected for checkout")
    @Size(min = 1, message = "At least one cart item must be selected for checkout")
    private List<Long> selectedCartItemIds;

    @NotNull(message = "User ID is required")
    private Long userAddressId;
}
