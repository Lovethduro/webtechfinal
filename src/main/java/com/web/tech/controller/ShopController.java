package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.service.ProductImageService;
import com.web.tech.service.UserImageService;
import com.web.tech.service.UserService;
import com.web.tech.service.ProductService;
import com.web.tech.model.Products;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ShopController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserImageService userImageService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductService productService;

    @GetMapping("/shop")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String shopPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        // Handle user authentication and profile picture
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Optional<User> userOptional = Optional.ofNullable(userService.findByEmail(auth.getName().toLowerCase()));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                String profilePicture = userImageService.getProfileImageBase64(user.getId());
                String contentType = userImageService.getProfileImageContentType(user.getId());
                model.addAttribute("profilePicture",
                        profilePicture != null && contentType != null
                                ? "data:" + contentType + ";base64," + profilePicture
                                : "/images/default-avatar.jpg");
            } else {
                model.addAttribute("user", null);
                model.addAttribute("profilePicture", "/images/default-avatar.jpg");
            }
        } else {
            model.addAttribute("user", null);
            model.addAttribute("profilePicture", "/images/default-avatar.jpg");
        }

        try {
            // Search products with the given criteria
            Page<Products> products = productService.searchProducts(name, minPrice, maxPrice, pageable);
            model.addAttribute("products", products);

            // Handle product images
            Map<Long, String> productImages = new HashMap<>();
            for (Products product : products.getContent()) {
                String imageBase64 = productImageService.getProductImageBase64(product.getId().toString());
                String contentType = productImageService.getProductImageContentType(product.getId().toString());
                productImages.put(product.getId(),
                        imageBase64 != null && contentType != null
                                ? "data:" + contentType + ";base64," + imageBase64
                                : "/images/default-product.jpg");
            }
            model.addAttribute("productImageBase64", productImages);

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // Return all products if there's an error in search parameters
            Page<Products> products = productService.getAllProducts(pageable);
            model.addAttribute("products", products);

            // Handle product images for fallback case
            Map<Long, String> productImages = new HashMap<>();
            for (Products product : products.getContent()) {
                String imageBase64 = productImageService.getProductImageBase64(product.getId().toString());
                String contentType = productImageService.getProductImageContentType(product.getId().toString());
                productImages.put(product.getId(),
                        imageBase64 != null && contentType != null
                                ? "data:" + contentType + ";base64," + imageBase64
                                : "/images/default-product.jpg");
            }
            model.addAttribute("productImageBase64", productImages);
        }

        model.addAttribute("pageTitle", "Shop Artworks");
        return "admin/user/shop";
    }


}