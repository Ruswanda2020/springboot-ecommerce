package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.UserAddressRequest;
import com.oneDev.ecommerce.model.response.OrderResponse;
import com.oneDev.ecommerce.model.response.UserAddressResponse;
import com.oneDev.ecommerce.service.UserAddressService;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/address")
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final UserInfoHelper userInfoHelper;

    @PostMapping
    public ResponseEntity<UserAddressResponse> createUserAddress(@Valid @RequestBody UserAddressRequest userAddressRequest) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        UserAddressResponse userAddressResponse = userAddressService
                .createUserAddress(userInfo.getUser().getUserId(), userAddressRequest);
        return ResponseEntity.ok(userAddressResponse);
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> findUserAddressByUserId(){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        List<UserAddressResponse> userAddressResponses = userAddressService
                .findUserAddressByUserId(userInfo.getUser().getUserId());
        return ResponseEntity.ok(userAddressResponses);
    }

    @PutMapping("/{userAddressId}")
    public ResponseEntity<UserAddressResponse> updateUserAddress(@PathVariable("userAddressId") Long userAddressId ,
                                                                    @Valid @RequestBody UserAddressRequest userAddressRequest) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        UserAddressResponse userAddressResponse = userAddressService.updateUserAddress(userAddressId, userAddressRequest);
        return ResponseEntity.ok(userAddressResponse);
    }

    @GetMapping("/{userAddressId}")
    public ResponseEntity<UserAddressResponse> findById(@PathVariable("userAddressId") Long userAddressId) {
        UserAddressResponse userAddressResponse = userAddressService.findById(userAddressId);
        return ResponseEntity.ok(userAddressResponse);
    }

    @DeleteMapping("/{userAddressId}")
    public ResponseEntity<Void> deleteById(@PathVariable("userAddressId") Long userAddressId) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
         userAddressService.deleteUserAddressById(userAddressId);
         return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userAddressId}/set-default")
    public ResponseEntity<UserAddressResponse> setDefault(@PathVariable("userAddressId") Long userAddressId) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        UserAddressResponse userAddressResponse = userAddressService.setDefault(userInfo.getUser().getUserId(), userAddressId);
        return ResponseEntity.ok(userAddressResponse);
    }
}
