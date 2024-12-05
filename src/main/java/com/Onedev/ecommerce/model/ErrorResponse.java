package com.Onedev.ecommerce.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private LocalDateTime timestamp;
}
