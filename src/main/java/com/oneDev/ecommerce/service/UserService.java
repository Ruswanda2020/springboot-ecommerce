package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.request.UserRegisterRequest;
import com.oneDev.ecommerce.model.request.UserUpdateRequest;
import com.oneDev.ecommerce.model.response.UserResponse;

public interface UserService {
    UserResponse register(UserRegisterRequest userRegisterRequest);
    UserResponse findById(Long userId);
    UserResponse findByUsernameOrEmail(String keyword);
    UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    void deleteById(Long userId);


}
