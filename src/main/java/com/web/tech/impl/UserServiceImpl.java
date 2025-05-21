package com.web.tech.impl;

import com.web.tech.model.User;
import com.web.tech.repository.UserRepository;
import com.web.tech.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    public UserServiceImpl(UserRepository userRepository
                         ) {
        this.userRepository = userRepository;

    }

    @Override
    @Transactional
    public User save(User user) {
        user.setEmail(user.getEmail().toLowerCase()); // Normalize email
        logger.debug("Saving user with email: {}", user.getEmail());
        return userRepository.save(user);

    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }



    @Override
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Returns List instead of Page
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findByRolesNotContaining("ADMIN", pageable);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Page<User> findByNameOrEmail(String query, Pageable pageable) {
        return userRepository.findByNameOrEmailAndRolesNotContaining(query, "ADMIN", pageable);
    }

    @Override
    @Transactional
    public User updateUser(User updatedUser) throws Exception {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new Exception("User not found with ID: " + updatedUser.getId()));
        // Check for email uniqueness (exclude current user)
        if (!existingUser.getEmail().equalsIgnoreCase(updatedUser.getEmail()) && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new Exception("Email already in use: " + updatedUser.getEmail());
        }

        // Check for phone uniqueness (exclude current user)
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().equals(existingUser.getPhone()) && userRepository.existsByPhone(updatedUser.getPhone())) {
            throw new Exception("Phone number already in use: " + updatedUser.getPhone());
        }

        if (existingUser.getRole().equals("ADMIN") && !updatedUser.getRole().equals("ADMIN")) {
            throw new Exception("Cannot change ADMIN role to non-ADMIN");
        }

        // Update fields
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setRole(updatedUser.getRole());


        // Save updated user
        return userRepository.save(existingUser);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    @Override
    public boolean validateEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent(); // Use case-insensitive lookup
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(newPassword); // Store password as plain text
        userRepository.save(user);
    }
    }


