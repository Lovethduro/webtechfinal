package com.web.tech.service;

import com.web.tech.model.ProductImages;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

public interface ProductImageService {
    ProductImages saveImage(String productId, MultipartFile file) throws IOException;
    Optional<ProductImages> getImage(String imageId);
    Optional<ProductImages> getImageByProductId(String productId);
    void deleteImage(String productId);
}

