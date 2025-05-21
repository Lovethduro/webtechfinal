package com.web.tech.service;

import com.web.tech.model.Cart;
import com.web.tech.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;

public interface OrderService {
    Orders createOrder(Long userId, Cart cart, String shippingAddress, String paymentMethod);
    void cancelOrder(String orderNumber, Long userId);
    BigDecimal getTotalSpent(Long userId);
    long countAllOrders();
    Map<String, Long> getOrdersByMonth(int months);
    Orders findById(Long id);
    Page<Orders> getAllOrders(Pageable pageable);
    void updateOrderStatus(Long orderId, String status);

}