package com.web.tech.service;

import com.web.tech.model.Cart;
import com.web.tech.model.Products;

public interface CartService {
    Cart getOrCreateCart(Long userId);
    void addToCart(Long userId, Long productId, int quantity);
    long getCartCount(Long userId);
    void clearCart(Long userId);
    void saveCart(Cart cart);
}