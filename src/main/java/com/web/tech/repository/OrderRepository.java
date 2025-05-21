package com.web.tech.repository;

import com.web.tech.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByOrderNumber(String orderNumber);
    List<Orders> findByUserId(Long userId);
}