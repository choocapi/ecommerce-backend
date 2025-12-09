package com.choocapi.ecommercebackend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.choocapi.ecommercebackend.dto.response.OrderResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.CategorySalesResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.InventoryStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.InventoryTransactionStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.ProductSalesResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.ProductsSalesStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.SalesStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.StatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.StatisticsRevenuePointResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.StatisticsSummaryResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.TimePointResponse;
import com.choocapi.ecommercebackend.entity.Order;
import com.choocapi.ecommercebackend.enums.OrderStatus;
import com.choocapi.ecommercebackend.enums.PaymentMethod;
import com.choocapi.ecommercebackend.enums.StatisticsRange;
import com.choocapi.ecommercebackend.mapper.UserMapper;
import com.choocapi.ecommercebackend.repository.InventoryTransactionRepository;
import com.choocapi.ecommercebackend.repository.OrderItemRepository;
import com.choocapi.ecommercebackend.repository.OrderRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.ReturnRequestRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    ReturnRequestRepository returnRequestRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    UserMapper userMapper;

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(StatisticsRange range) {
        // Summary: Tổng tất cả (không cần range)
        StatisticsSummaryResponse summary = getSummary();

        // Revenue: Chỉ có khi có range parameter
        List<StatisticsRevenuePointResponse> revenue = null;
        if (range != null) {
            revenue = getRevenuePoints(range);
        }

        return StatisticsResponse.builder()
                .range(range != null ? range.name() : null)
                .summary(summary)
                .revenue(revenue)
                .build();
    }

    private StatisticsSummaryResponse getSummary() {
        // Sử dụng repository methods để tính toán trực tiếp trong database
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.getTotalOrdersCount();
        
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Long completedOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        Long newCustomers = userRepository.count();

        return StatisticsSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .newCustomers(newCustomers)
                .build();
    }

    private List<StatisticsRevenuePointResponse> getRevenuePoints(StatisticsRange range) {
        Instant startDate = getStartDate(range);
        List<Object[]> results = orderRepository.getRevenueByDate(startDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        return results.stream()
                .map(result -> {
                    // Date is returned as String from PostgreSQL DATE()::text
                    String dateStr = result[0].toString();
                    LocalDate date = LocalDate.parse(dateStr);
                    BigDecimal total = (BigDecimal) result[1];
                    return StatisticsRevenuePointResponse.builder()
                            .label(date.format(formatter))
                            .total(total)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders(int limit) {
        // Chỉ lấy các đơn hàng đã hoàn thành (DELIVERED)
        List<Order> orders = orderRepository.findRecentDeliveredOrders(
                OrderStatus.DELIVERED,
                PageRequest.of(0, limit)
        );
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getUser() != null ? order.getUser().getId() : null)
                .user(order.getUser() != null ? userMapper.toResponse(order.getUser()) : null)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingWard(order.getShippingWard())
                .shippingDistrict(order.getShippingDistrict())
                .shippingCity(order.getShippingCity())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .orderedAt(order.getOrderedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .items(null) // Không cần items cho recent orders
                .build();
    }

    @Transactional(readOnly = true)
    public SalesStatisticsResponse getSalesStatistics(StatisticsRange range) {
        // Cards: Tổng tất cả - sử dụng repository methods
        BigDecimal totalSales = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.getTotalOrdersCount();
        Long completedOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        Long returnRequests = returnRequestRepository.count();

        BigDecimal returnRate = totalOrders > 0
                ? BigDecimal.valueOf(returnRequests)
                        .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Payment method breakdown: Tổng tất cả - sử dụng repository method
        Map<String, BigDecimal> paymentMethodBreakdown = new HashMap<>();
        List<Object[]> paymentResults = orderRepository.getRevenueByPaymentMethod();
        for (Object[] result : paymentResults) {
            PaymentMethod method = (PaymentMethod) result[0];
            BigDecimal total = (BigDecimal) result[1];
            paymentMethodBreakdown.put(method.name(), total);
        }

        // Orders timeline: Chỉ có khi có range
        List<TimePointResponse> ordersTimeline = null;
        if (range != null) {
            ordersTimeline = getOrdersTimeline(range);
        }

        return SalesStatisticsResponse.builder()
                .totalSales(totalSales)
                .completedOrders(completedOrders)
                .pendingOrders(pendingOrders)
                .cancelledOrders(cancelledOrders)
                .returnRequests(returnRequests)
                .returnRate(returnRate)
                .paymentMethodBreakdown(paymentMethodBreakdown)
                .ordersTimeline(ordersTimeline)
                .build();
    }

    private List<TimePointResponse> getOrdersTimeline(StatisticsRange range) {
        Instant startDate = getStartDate(range);
        List<Object[]> results = orderRepository.getOrdersCountByDate(startDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        return results.stream()
                .map(result -> {
                    // Date is returned as String from PostgreSQL DATE()::text
                    String dateStr = result[0].toString();
                    LocalDate date = LocalDate.parse(dateStr);
                    Long count = ((Number) result[1]).longValue();
                    return TimePointResponse.builder()
                            .label(date.format(formatter))
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductsSalesStatisticsResponse getProductsSalesStatistics() {
        // Top products: Tổng tất cả - sử dụng repository method
        List<Object[]> topProductsResults = orderItemRepository.getTopProductsByRevenue();
        
        List<ProductSalesResponse> topProducts = topProductsResults.stream()
                .limit(5)
                .map(result -> ProductSalesResponse.builder()
                        .productId(((Number) result[0]).longValue())
                        .productName((String) result[1])
                        .totalQuantity(((Number) result[2]).longValue())
                        .totalRevenue((BigDecimal) result[3])
                        .build())
                .collect(Collectors.toList());

        // Category performance: Tổng tất cả - sử dụng repository method
        List<Object[]> categoryResults = orderItemRepository.getCategoryPerformance();
        
        List<CategorySalesResponse> categoryPerformance = categoryResults.stream()
                .map(result -> CategorySalesResponse.builder()
                        .categoryId(((Number) result[0]).longValue())
                        .categoryName((String) result[1])
                        .totalRevenue((BigDecimal) result[2])
                        .build())
                .collect(Collectors.toList());

        return ProductsSalesStatisticsResponse.builder()
                .topProducts(topProducts)
                .categoryPerformance(categoryPerformance)
                .build();
    }

    @Transactional(readOnly = true)
    public InventoryStatisticsResponse getInventoryStatistics() {
        // Sử dụng repository methods để tính toán trực tiếp trong database
        Long totalProducts = productRepository.getTotalProductsCount();
        Long publishedProducts = productRepository.getPublishedProductsCount();
        Long lowStockProducts = productRepository.getLowStockProductsCount();
        BigDecimal totalInventoryValue = productRepository.getTotalInventoryValue();

        // Recent transactions: Top 10
        List<com.choocapi.ecommercebackend.entity.InventoryTransaction> transactions = 
                inventoryTransactionRepository.findAll(
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
                ).getContent();

        List<InventoryTransactionStatisticsResponse> recentTransactions = transactions.stream()
                .map(t -> InventoryTransactionStatisticsResponse.builder()
                        .id(t.getId())
                        .productName(t.getProduct().getName())
                        .type(t.getType().name())
                        .quantity(t.getQuantity())
                        .price(t.getPrice())
                        .supplierName(t.getSupplier() != null ? t.getSupplier().getName() : null)
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return InventoryStatisticsResponse.builder()
                .totalProducts(totalProducts)
                .publishedProducts(publishedProducts)
                .lowStockProducts(lowStockProducts)
                .totalInventoryValue(totalInventoryValue)
                .recentTransactions(recentTransactions)
                .build();
    }

    private Instant getStartDate(StatisticsRange range) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        
        switch (range) {
            case LAST_7_DAYS:
                startDate = now.minusDays(7);
                break;
            case LAST_30_DAYS:
                startDate = now.minusDays(30);
                break;
            case LAST_90_DAYS:
                startDate = now.minusDays(90);
                break;
            default:
                startDate = now.minusDays(30);
        }
        
        return startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

}
