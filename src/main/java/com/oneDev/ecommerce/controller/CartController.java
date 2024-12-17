package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.AddToCartRequest;
import com.oneDev.ecommerce.model.request.UpdateItemRequest;
import com.oneDev.ecommerce.model.response.CartItemResponse;
import com.oneDev.ecommerce.service.CartService;
import com.oneDev.ecommerce.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
@SecurityRequirement(name = "Bearer")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UserInfoHelper userInfoHelper;


    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(@Valid @RequestBody AddToCartRequest addToCartRequest){
        log.info("ProductId : {}", addToCartRequest.getProductId());
        log.info("Quantity : {}", addToCartRequest.getQuantity());

        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        cartService.addItemToCart(userInfo.getUser().getUserId(),
                                  addToCartRequest.getProductId(),
                                  addToCartRequest.getQuantity());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/items")
    public ResponseEntity<Void> updateCartItemQuantity(@Valid @RequestBody UpdateItemRequest updateItemRequest){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();

        cartService.updateCartItemQuantity(userInfo.getUser().getUserId(),
                updateItemRequest.getProductId(),
                updateItemRequest.getQuantity());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItemById(@PathVariable("cartItemId") Long cartItemId){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        cartService.removeItemFromCart(userInfo.getUser().getUserId(), cartItemId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        List<CartItemResponse> itemResponses = cartService.getCartItems(userInfo.getUser().getUserId());
        return ResponseEntity.ok(itemResponses);
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart(){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        cartService.clearCart(userInfo.getUser().getUserId());
        return ResponseEntity.noContent().build();
    }




}
