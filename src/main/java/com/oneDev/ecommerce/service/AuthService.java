package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.request.AuthRequest;
import com.oneDev.ecommerce.model.UserInfo;

public interface AuthService {

    UserInfo authenticate(AuthRequest authRequest);
}
