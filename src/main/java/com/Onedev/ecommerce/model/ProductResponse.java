package com.Onedev.ecommerce.model;

import lombok.*;


import java.math.BigDecimal;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    String name;
    BigDecimal price;
    String description;
}
