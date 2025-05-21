package com.web.tech.repository;

import com.web.tech.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    long countByUserId(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}