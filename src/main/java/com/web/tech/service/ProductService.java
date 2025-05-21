package com.web.tech.service;

import com.web.tech.model.Clients;
import com.web.tech.model.Products;
import com.web.tech.repository.ProductRepository;
import com.web.tech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.web.tech.repository.ClientRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    public Optional<Products> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;


    public void saveProduct(Products product, MultipartFile image) throws IOException {
        // Save product to PostgreSQL
        product = productRepository.save(product);
        // Save image to MongoDB
        if (image != null && !image.isEmpty()) {
            System.out.println("Saving image for product ID: " + product.getId());
            productImageService.saveImage(String.valueOf(product.getId()), image);
        } else {
            System.err.println("No image provided for product ID: " + product.getId());
        }
    }

    public List<Products> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Products> searchProducts(String name, Double minPrice, Double maxPrice) {
        // Convert Double to BigDecimal for database operations
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        // Validate price range if both are provided
        if (min != null && max != null) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
            }
        }

        // Perform appropriate search based on provided parameters
        if (name != null && !name.trim().isEmpty() && min != null && max != null) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                    name.trim(), min, max);
        } else if (name != null && !name.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(name.trim());
        } else if (min != null && max != null) {
            return productRepository.findByPriceBetween(min, max);
        }

        // If no search criteria provided, return all products
        return getAllProducts();
    }

    public Page<Products> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Products> searchProducts(String name, Double minPrice, Double maxPrice, Pageable pageable) {
        // Validate name
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }

        // Convert Double to BigDecimal for database operations
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        // Validate price range if both are provided
        if (min != null && max != null) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
            }
        }

        // Perform appropriate search based on provided parameters
        if (name != null && !name.trim().isEmpty() && min != null && max != null) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                    name.trim(), min, max, pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else if (min != null && max != null) {
            return productRepository.findByPriceBetween(min, max, pageable);
        }

        // If no search criteria provided, return all products
        return productRepository.findAll(pageable);


    }

    public void deleteProduct(Long id) {
        Optional<Products> productOptional = productRepository.findById(id);
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }

        productRepository.deleteById(id);

        String imageDeletionError = null;
        try {
            productImageService.deleteImage(String.valueOf(id));
        } catch (IOException e) {
            imageDeletionError = "Product deleted, but failed to delete associated image: " + e.getMessage();
        }

        if (imageDeletionError != null) {
            throw new IllegalStateException(imageDeletionError);
        }
    }

    public List<Clients> getAllClients() {
        return clientRepository.findAll();
    }

    public Long countAllArtworks() {
        return productRepository.count(); // Fixed: Use productRepository instead of artworkRepository
    }

    public Long countAllClients() {
        Long count = userRepository.countByRoleNot("ADMIN");
        System.out.println("Total non-admin users count: " + count);
        if (count == null || count == 0) {
            System.err.println("Warning: No non-admin users found in the database");
        }
        return count != null ? count : 0L;
    }

    public String getProductImageBase64(String productId) throws IOException {
        return productImageService.getProductImageBase64(productId);
    }

    public String getProductImageContentType(String productId) {
        return productImageService.getProductImageContentType(productId);
    }


    public String getProductImage(Long productId) {
        String image = productImageService.getProductImage(productId);
        return image != null ? image : "/images/default-product.jpg";
    }

    public Map<String, Long> getArtworksByCategory() {
        List<Object[]> results = productRepository.getArtworksByCategory();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            categoryCounts.put(category, count);
        }
        return categoryCounts;
    }

    public void updateProduct(Products product) {
        productRepository.save(product);

    }

    public Long countOutOfStock() {
        return productRepository.countByStockLessThanEqual(0);
    }

    public Page<Products> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }


}