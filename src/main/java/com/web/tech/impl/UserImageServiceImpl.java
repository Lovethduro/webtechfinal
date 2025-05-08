package com.web.tech.impl;

import com.web.tech.model.User;
import com.web.tech.model.UserImage;
import com.web.tech.repository.UserImageRepository;
import com.web.tech.repository.UserRepository;
import com.web.tech.service.UserImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserImageServiceImpl implements UserImageService {

    private final UserImageRepository userImageRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserImageServiceImpl(UserImageRepository userImageRepository, UserRepository userRepository) {
        this.userImageRepository = userImageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserImage saveImage(String userId, MultipartFile file) throws IOException {
        // 1. First, check if the user exists in PostgreSQL
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Delete existing image if it exists (to avoid duplicates)
        if (userImageRepository.existsByUserId(userId)) {
            userImageRepository.deleteByUserId(userId);
        }

        // Log file details
        System.out.println("Uploading image for user: " + userId);
        System.out.println("File content type: " + file.getContentType());
        System.out.println("File size: " + file.getSize());

        // Check if the file is empty
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Convert file to byte array
        byte[] imageData = file.getBytes();
        System.out.println("Image data size: " + imageData.length); // Log the byte array size

        // Create a new UserImage object
        UserImage userImage = new UserImage(userId, imageData, file.getContentType());

        // Log the file size (in bytes)
        System.out.println("File size: " + userImage.getFileSize());

        // Save the image to MongoDB
        UserImage savedImage = userImageRepository.save(userImage);

        // Update the user with the image reference
        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
        if (userOptional.isPresent()) {
            User userr = userOptional.get();
            user.setProfileImageId(savedImage.getId());
            userRepository.save(user);
        }

        return savedImage;
    }

    @Override
    public Optional<UserImage> getImage(String imageId) {
        return userImageRepository.findById(imageId);
    }

    @Override
    public Optional<UserImage> getImageByUserId(String userId) {
        return userImageRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteImage(String userId) {
        // 1. First find the user in PostgreSQL
        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 2. Remove the image reference from the user
            user.setProfileImageId(null);
            userRepository.save(user);
        }

        // 3. Delete the image from MongoDB
        userImageRepository.deleteByUserId(userId);
    }
}