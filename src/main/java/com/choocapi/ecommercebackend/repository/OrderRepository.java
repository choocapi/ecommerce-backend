package com.choocapi.ecommercebackend.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderedAtDesc(User user);
    Optional<Order> findByVnPayOrderId(String vnPayOrderId);
    Optional<Order> findByMomoOrderId(String momoOrderId);
    Optional<Order> findByZalopayOrderId(String zalopayOrderId);

    // Statistics queries
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    Long getTotalOrdersCount();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query(value = """
        SELECT DATE(o.ordered_at)::text as date, SUM(o.total_amount) as total
        FROM orders o
        WHERE o.ordered_at >= CAST(:startDate AS TIMESTAMP)
        GROUP BY DATE(o.ordered_at)
        ORDER BY DATE(o.ordered_at) ASC
        """, nativeQuery = true)
    List<Object[]> getRevenueByDate(@Param("startDate") Instant startDate);

    @Query(value = """
        SELECT DATE(o.ordered_at)::text as date, COUNT(o.id) as count
        FROM orders o
        WHERE o.ordered_at >= CAST(:startDate AS TIMESTAMP)
        GROUP BY DATE(o.ordered_at)
        ORDER BY DATE(o.ordered_at) ASC
        """, nativeQuery = true)
    List<Object[]> getOrdersCountByDate(@Param("startDate") Instant startDate);

    @Query("""
        SELECT o.paymentMethod, SUM(o.totalAmount) as total
        FROM Order o
        WHERE o.paymentMethod IS NOT NULL
        GROUP BY o.paymentMethod
        """)
    List<Object[]> getRevenueByPaymentMethod();

    // Get recent delivered orders
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderedAt DESC")
    List<Order> findRecentDeliveredOrders(@Param("status") OrderStatus status, Pageable pageable);
}
