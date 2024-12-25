package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.UserUpdateRequest;
import com.oneDev.ecommerce.model.response.UserResponse;
import com.oneDev.ecommerce.service.UserService;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;
    private final UserInfoHelper userInfoHelper;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        UserResponse response = UserResponse.from(userInfo.getUser(), userInfo.getRoles());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("userId") Long userId,
                                                   @Valid @RequestBody UserUpdateRequest request){

        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        if (!Objects.equals(userInfo.getUser().getUserId(), userId) &&
                userInfo.getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "user " + userInfo.getUsername() + " is not allowed to update");
        }


        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId){

        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        if (!Objects.equals(userInfo.getUser().getUserId(), userId) &&
                userInfo.getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "user " + userInfo.getUsername() + " is not allowed to delete");
        }

        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

}
