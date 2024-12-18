package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.request.UserAddressRequest;
import com.oneDev.ecommerce.model.response.UserAddressResponse;

import java.util.List;

public interface UserAddressService {

    UserAddressResponse createUserAddress(Long userId, UserAddressRequest userAddressRequest);
    List<UserAddressResponse> findUserAddressByUserId(Long userId);
    UserAddressResponse updateUserAddress(Long addressId, UserAddressRequest userAddressRequest);
    UserAddressResponse findById(Long userAddressId);
    void deleteUserAddressById(Long userAddressId);
    UserAddressResponse setDefault(Long userId, Long addressId);
}
