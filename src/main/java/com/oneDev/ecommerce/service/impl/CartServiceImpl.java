package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Cart;
import com.oneDev.ecommerce.entity.CartItem;
import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.response.CartItemResponse;
import com.oneDev.ecommerce.repository.CartItemRepository;
import com.oneDev.ecommerce.repository.CartRepository;
import com.oneDev.ecommerce.repository.ProductRepository;
import com.oneDev.ecommerce.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override @Transactional
    public void addItemToCart(Long userId, Long productId, int quantityToAdd) {
        // Ambil keranjang pengguna berdasarkan userId; buat keranjang baru jika tidak ada
        Cart userCart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Ambil produk berdasarkan productId, jika tidak ditemukan, lempar exception
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Product with ID " + productId + " not found"));

        // Validasi apakah produk adalah milik user sendiri
        if (product.getUserId() != null && product.getUserId().equals(userId)) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST,
                    "Cannot add your own product to cart.");
        }

        //validasi jika stoknya productnya lebih kecil == 0 lempar exception
        if (product.getStockQuantity() <= 0){
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Product stock is equals or below zero");
        }

        // Periksa apakah produk sudah ada di keranjang
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductId(userCart.getCartId(), productId);

        if (existingCartItem.isPresent()) {
            // Jika produk sudah ada, update kuantitas
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantityToAdd);
            cartItemRepository.save(cartItem);
        } else {
            // Jika produk belum ada, buat item baru di keranjang
            CartItem newCartItem = CartItem.builder()
                    .cartId(userCart.getCartId())
                    .productId(productId)
                    .quantity(quantityToAdd)
                    .price(product.getPrice())
                    .build();
            cartItemRepository.save(newCartItem);
        }
    }

    @Override @Transactional
    public void updateCartItemQuantity(Long userId, Long productId, int newQuantity) {
        // Ambil keranjang pengguna berdasarkan userId
        Cart userCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Cart not found for user with ID: " + userId));

        // Periksa apakah item ada di keranjang
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductId(userCart.getCartId(), productId);

        if (existingCartItem.isEmpty()) {
            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                    "Product  " + productId + " is not yet added to cart.");
        }

        CartItem cartItem = existingCartItem.get();

        // Validasi apakah item milik keranjang user
        if (!cartItem.getCartId().equals(userCart.getCartId())) {
            throw new ApplicationException(ExceptionType.FORBIDDEN,
                    "Cart item doesn't belong to user's cart.");
        }

        if (newQuantity <= 0) {
            // Jika kuantitas baru kurang dari atau sama dengan 0, hapus item dari keranjang
            cartItemRepository.deleteById(cartItem.getId());
        } else {
            // Jika kuantitas valid, update kuantitas item
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Override @Transactional
    public void removeItemFromCart(Long userId, Long cartItemId) {
        // Ambil keranjang pengguna
        Cart userCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Cart not found for user with ID: " + userId));

        // Cari item berdasarkan cartItemId
        Optional<CartItem> existingCartItem = cartItemRepository.findById(cartItemId);

        if (existingCartItem.isEmpty()) {
            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Cart item not found.");
        }

        CartItem cartItem = existingCartItem.get();
        if (!cartItem.getCartId().equals(userCart.getCartId())) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "Cart item doesn't belong to user's cart.");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    @Override @Transactional
    public void clearCart(Long userId) {
        // Ambil keranjang pengguna
        Cart userCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Cart not found for user with ID: " + userId));
        // Hapus semua item dari keranjang
        cartItemRepository.deleteAllByCartId(userCart.getCartId());
    }

    @Override
    public List<CartItemResponse> getCartItems(Long userId) {
        // Ambil semua item keranjang pengguna
        List<CartItem> cartItems = cartItemRepository.getUserCartItems(userId);
        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        // Ambil ID produk dari item keranjang
        List<Long> productIds = cartItems
                .stream()
                .map(CartItem::getProductId)
                .toList();

        // Ambil detail produk berdasarkan ID
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // Map item keranjang ke response
        return cartItems.stream()
                .map(cartItem -> {
                    Product product = productMap.get(cartItem.getProductId());
                    if (product == null) {
                       throw new ApplicationException(
                                ExceptionType.RESOURCE_NOT_FOUND,
                                "product not found for user ID: " + cartItem.getProductId());
                    }
                    return CartItemResponse.from(cartItem, product);
                }).toList();
    }
}
