package com.web.tech.service;


import com.web.tech.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    boolean emailExists(String email);
    User findByEmail(String email);  // Added for completeness
    boolean phoneExists(String phone);
    List<User> getAllUsers();

    Page<User> getAllUsers(Pageable pageable);
    Optional<User> getUserById(Long id);
    Page<User> findByNameOrEmail(String query, Pageable pageable);
    User updateUser(User user) throws Exception;;
    User findById(Long id);
    void deleteUser(Long id);

    boolean validateEmail(String email); // Added
    void resetPassword(String email, String newPassword);
}