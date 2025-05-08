package com.web.tech.service;


import com.web.tech.model.User;

public interface UserService {
    User save(User user);
    boolean emailExists(String email);
    User findByEmail(String email);  // Added for completeness

    boolean phoneExists(String phone);


}