package com.web.tech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test-db")
    public String testDatabaseConnection() {
        try {
            // This query is just an example; you can customize it based on your DB schema
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            return "Database is connected. Rows count: " + count;
        } catch (Exception e) {
            return "Database connection failed: " + e.getMessage();
        }
    }
}
