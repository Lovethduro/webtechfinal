package com.web.tech.impl;

import com.web.tech.model.Cart;
import com.web.tech.model.Products;
import com.web.tech.model.User;
import com.web.tech.repository.CartRepository;
import com.web.tech.service.CartService;
import com.web.tech.service.ProductService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.getUserById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    Cart cart = new Cart(user);
                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Cart cart = getOrCreateCart(userId);
        Products product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        cart.addProduct(product, quantity);
        cartRepository.save(cart);
    }

    @Override
    public long getCartCount(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cart.getItems().stream().mapToLong(item -> item.getQuantity()).sum())
                .orElse(0L);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }
}