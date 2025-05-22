package com.web.tech.repository;

import com.web.tech.model.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductRepository extends JpaRepository<Products, Long> {

    List<Products> findByNameContainingIgnoreCase(String name);

    Page<Products> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Products> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    Page<Products> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    List<Products> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, BigDecimal minPrice, BigDecimal maxPrice);

    Page<Products> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Optional: Custom query for flexibility
    @Query("SELECT p FROM Products p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Products> findByNameAndPriceRange(
            @Param("name") String name,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT p.category AS category, COUNT(p) AS count FROM Products p GROUP BY p.category")
    List<Object[]> getArtworksByCategory();

    @Query("SELECT COUNT(p) FROM Products p WHERE p.stock <= :stock")
    Long countByStockLessThanEqual(@Param("stock") int stock);

}