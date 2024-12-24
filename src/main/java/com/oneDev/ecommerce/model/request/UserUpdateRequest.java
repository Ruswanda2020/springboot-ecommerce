package com.oneDev.ecommerce.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.*\\s).+$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace"
    )
    private String newPassword;

    private String currentPassword;
}
