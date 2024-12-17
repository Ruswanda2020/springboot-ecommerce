package com.oneDev.ecommerce.service;

import com.oneDev.ecommerce.model.response.CartItemResponse;

import java.util.List;

public interface CartService {

    void addItemToCart(Long userId, Long productId, int quantity);
    void updateCartItemQuantity(Long userId, Long productId, int quantity);
    void removeItemFromCart(Long userId, Long cartItemId);
    void clearCart(Long userId);
    List<CartItemResponse> getCartItems(Long userId);
}
