package com.Onedev.ecommerce.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

     @NotBlank(message = "Product name must not be empty.")
     @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters.")
     String name;

     @NotNull(message = "Price must not be null.")
     @Positive(message = "Price must be greater than 0.")
     @Digits(integer = 10, fraction = 2, message = "Price must be a valid decimal number with up to 10 digits and 2 decimal places.")
     BigDecimal price;

     @Size(max = 1000, message = "Description must not exceed 1000 characters.")
     String description;
}
