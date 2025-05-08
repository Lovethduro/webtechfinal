package com.web.tech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @NotNull(message = "Total amount is required")
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @NotBlank(message = "Shipping address is required")
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Constructors
    public Orders() {
        this.orderDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        // Generate a unique order number based on timestamp
        this.orderNumber = "ORD-" + System.currentTimeMillis();
    }

    // Method to add item to order
    public void addItem(Products product, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(this);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        items.add(orderItem);

        // Update total amount
        calculateTotal();
    }

    // Method to calculate total order amount
    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Method to create order from cart
    public static Orders createFromCart(Cart cart, String shippingAddress, String paymentMethod) {
        Orders order = new Orders();
        order.setUser(cart.getUser());
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);

        // Convert cart items to order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(cart.getTotalPrice());
        return order;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotal();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}