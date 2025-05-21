package com.web.tech.impl;

import com.web.tech.model.User;
import com.web.tech.model.UserImage;
import com.web.tech.repository.UserImageRepository;
import com.web.tech.repository.UserRepository;
import com.web.tech.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;


    // Explicit constructor for dependency injection
    public UserRegistrationServiceImpl(UserRepository userRepository,
                                       UserImageRepository userImageRepository
                                       ) {
        this.userRepository = userRepository;
        this.userImageRepository = userImageRepository;

    }

    @Override
    @Transactional
    public User registerUser(User user, MultipartFile profileImage) throws IOException {
        logger.debug("Starting user registration...");



        logger.debug("Saving user to database...");
        User savedUser = userRepository.save(user);
        logger.debug("Saved user with ID: {}", savedUser.getId());

        // 2. Handle image
        if (!profileImage.isEmpty()) {
            logger.debug("Processing profile image...");
            UserImage userImage = new UserImage();
            userImage.setUserId(savedUser.getId().toString());
            userImage.setImageData(profileImage.getBytes());
            userImage.setContentType(profileImage.getContentType());

            UserImage savedImage = userImageRepository.save(userImage);
            logger.debug("Saved image with ID: {}", savedImage.getId());

            savedUser.setProfileImageId(savedImage.getId());
            userRepository.save(savedUser);
        }

        logger.info("Completed registration for user ID: {}", savedUser.getId());
        return savedUser;
    }
}