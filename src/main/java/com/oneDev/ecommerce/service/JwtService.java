package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.UserInfo;

public interface JwtService {

    String generateToken(UserInfo userInfo);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
}
