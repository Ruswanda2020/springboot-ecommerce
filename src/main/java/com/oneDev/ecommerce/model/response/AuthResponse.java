package com.oneDev.ecommerce.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oneDev.ecommerce.entity.Role;
import com.oneDev.ecommerce.model.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthResponse implements Serializable {

    private String token;
    private Long userId;
    private String userName;
    private String email;
    private List<String> roles;

    public static AuthResponse from(UserInfo userInfo, String token) {
        return AuthResponse.builder()
                .token(token)
                .userId(userInfo.getUser().getUserId())
                .userName(userInfo.getUsername())
                .email(userInfo.getUser().getEmail())
                .roles(userInfo.getRoles().stream().map(Role::getName).toList())
                .build();
    }

}
