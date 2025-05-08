package com.web.tech.repository;

import com.web.tech.model.ProductImages;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface ProductImageRepository extends MongoRepository<ProductImages, String> {

    // Find by PostgreSQL product ID reference
    Optional<ProductImages> findByProductId(String productId);

    // Find all images for multiple products
    List<ProductImages> findAllByProductIdIn(List<String> productIds);

    // Check if image exists for product
    boolean existsByProductId(String productId);

    // Delete by product reference
    void deleteByProductId(String productId);
}