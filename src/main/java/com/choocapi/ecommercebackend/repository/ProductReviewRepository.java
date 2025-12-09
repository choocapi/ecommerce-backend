package com.choocapi.ecommercebackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.ProductReview;
import com.choocapi.ecommercebackend.entity.User;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductAndIsHiddenFalseOrderByCreatedAtDesc(Product product);

    List<ProductReview> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
            SELECT COALESCE(AVG(pr.rating), 0)
            FROM ProductReview pr
            WHERE pr.product = :product
              AND (pr.isHidden IS NULL OR pr.isHidden = false)
            """)
    Double findAverageRatingByProduct(@Param("product") Product product);

    @Query("""
            SELECT COUNT(pr)
            FROM ProductReview pr
            WHERE pr.product = :product
              AND (pr.isHidden IS NULL OR pr.isHidden = false)
            """)
    Long countVisibleReviewsByProduct(@Param("product") Product product);
}
