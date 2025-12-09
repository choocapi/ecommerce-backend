package com.choocapi.ecommercebackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    // Statistics queries
    @Query("""
        SELECT oi.product.id, oi.product.name, 
               SUM(oi.quantity) as totalQuantity, 
               SUM(oi.totalPrice) as totalRevenue
        FROM OrderItem oi
        GROUP BY oi.product.id, oi.product.name
        ORDER BY totalRevenue DESC
        """)
    List<Object[]> getTopProductsByRevenue();

    @Query("""
        SELECT oi.product.category.id, oi.product.category.name, 
               SUM(oi.totalPrice) as totalRevenue
        FROM OrderItem oi
        WHERE oi.product.category IS NOT NULL
        GROUP BY oi.product.category.id, oi.product.category.name
        ORDER BY totalRevenue DESC
        """)
    List<Object[]> getCategoryPerformance();
}
