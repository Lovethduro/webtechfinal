package com.web.tech.repository;

import com.web.tech.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT COUNT(*) > 0 FROM clients WHERE email = :email", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    User findByPhone(String phone);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> findByNameOrEmail(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role <> :role")
    Page<User> findByRolesNotContaining(@Param("role") String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND u.role <> :role")
    Page<User> findByNameOrEmailAndRolesNotContaining(@Param("query") String query, @Param("role") String role, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role <> :role")
    Long countByRoleNot(@Param("role") String role);


}