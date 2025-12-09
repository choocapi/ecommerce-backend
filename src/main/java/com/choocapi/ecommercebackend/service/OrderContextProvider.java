package com.choocapi.ecommercebackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.context.OrderContext;
import com.choocapi.ecommercebackend.dto.context.OrderSummary;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.entity.OrderItem;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.repository.OrderItemRepository;
import com.choocapi.ecommercebackend.repository.OrderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderContextProvider {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;

    /**
     * Get order context for an authenticated user
     * Returns recent orders with sanitized data (no sensitive payment info)
     * 
     * @param user The authenticated user
     * @param query Optional query string for filtering (currently unused, returns all user orders)
     * @return OrderContext containing user's orders
     */
    public OrderContext getOrderContext(User user, String query) {
        if (user == null) {
            return OrderContext.builder()
                    .orders(List.of())
                    .totalOrders(0)
                    .build();
        }

        // Get user's orders sorted by most recent first
        List<Order> orders = orderRepository.findByUserOrderByOrderedAtDesc(user);
        
        // Map to sanitized summaries
        List<OrderSummary> orderSummaries = orders.stream()
                .map(this::sanitizeOrderData)
                .collect(Collectors.toList());
        
        return OrderContext.builder()
                .orders(orderSummaries)
                .totalOrders(orderSummaries.size())
                .build();
    }

    /**
     * Get a specific order by ID for an authenticated user
     * Verifies the order belongs to the user before returning
     * 
     * @param orderId The order ID to retrieve
     * @param user The authenticated user
     * @return OrderSummary if found and belongs to user, null otherwise
     */
    public OrderSummary getOrderById(String orderId, User user) {
        if (orderId == null || user == null) {
            return null;
        }

        return orderRepository.findById(orderId)
                .filter(order -> order.getUser().getId().equals(user.getId()))
                .map(this::sanitizeOrderData)
                .orElse(null);
    }

    /**
     * Get recent orders for a user with a limit
     * 
     * @param user The authenticated user
     * @param limit Maximum number of orders to return
     * @return List of sanitized order summaries
     */
    public List<OrderSummary> getRecentOrders(User user, int limit) {
        if (user == null || limit <= 0) {
            return List.of();
        }

        List<Order> orders = orderRepository.findByUserOrderByOrderedAtDesc(user);
        
        return orders.stream()
                .limit(limit)
                .map(this::sanitizeOrderData)
                .collect(Collectors.toList());
    }

    /**
     * Sanitize order data by removing sensitive payment information
     * Converts Order entity to OrderSummary DTO without payment gateway IDs
     * 
     * @param order The order entity to sanitize
     * @return OrderSummary with safe data only
     */
    public OrderSummary sanitizeOrderData(Order order) {
        if (order == null) {
            return null;
        }

        // Count order items
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        int itemCount = items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        // Build summary without sensitive payment information
        // Excludes: vnPayOrderId, momoOrderId, zalopayOrderId
        return OrderSummary.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .itemCount(itemCount)
                .build();
    }
}
