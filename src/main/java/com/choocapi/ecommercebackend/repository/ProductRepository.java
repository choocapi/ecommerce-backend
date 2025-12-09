package com.choocapi.ecommercebackend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Category;
import com.choocapi.ecommercebackend.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    Optional<Product> findBySlug(String slug);
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category IN :categories")
    Page<Product> findByCategories(@Param("categories") List<Category> categories, Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(p) FROM Product p")
    Long getTotalProductsCount();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isPublished = true")
    Long getPublishedProductsCount();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity <= 10")
    Long getLowStockProductsCount();

    @Query("""
        SELECT COALESCE(SUM(p.price * p.quantity), 0)
        FROM Product p
        WHERE p.quantity IS NOT NULL AND p.price IS NOT NULL
        """)
    BigDecimal getTotalInventoryValue();
}
