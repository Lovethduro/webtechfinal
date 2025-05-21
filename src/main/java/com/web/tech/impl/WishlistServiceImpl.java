package com.web.tech.impl;

import com.web.tech.model.WishlistItem;
import com.web.tech.model.User;
import com.web.tech.model.Products;
import com.web.tech.repository.WishlistRepository;
import com.web.tech.service.UserService;
import com.web.tech.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserService userService;

    @Override
    public long countWishlistItemsByUser(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }

    @Transactional
    @Override
    public void addItem(Long userId, Products product) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, product.getId())) {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            WishlistItem item = new WishlistItem(user, product);
            wishlistRepository.save(item);
        }
    }
}