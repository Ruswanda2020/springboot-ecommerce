package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.AuthRequest;
import com.oneDev.ecommerce.model.UserInfo;

public interface AuthService {

    UserInfo authenticate(AuthRequest authRequest);
}
