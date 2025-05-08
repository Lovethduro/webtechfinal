package com.web.tech.repository;


import com.web.tech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT COUNT(*) > 0 FROM clients WHERE email = :email", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    Optional<User> findByEmail(String email);

    boolean existsByphone(String email);
}