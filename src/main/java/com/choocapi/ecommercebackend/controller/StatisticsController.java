package com.choocapi.ecommercebackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.OrderResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.InventoryStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.ProductsSalesStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.SalesStatisticsResponse;
import com.choocapi.ecommercebackend.dto.response.statistics.StatisticsResponse;
import com.choocapi.ecommercebackend.enums.StatisticsRange;
import com.choocapi.ecommercebackend.service.StatisticsService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsController {

    StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(
            @RequestParam(required = false) String range) {
        StatisticsRange statisticsRange = null;
        if (range != null) {
            try {
                statisticsRange = StatisticsRange.valueOf(range.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid range, will use null (no revenue data)
            }
        }
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getStatistics(statisticsRange)));
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getRecentOrders(limit)));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesStatisticsResponse>> getSalesStatistics(
            @RequestParam(required = false) String range) {
        StatisticsRange statisticsRange = null;
        if (range != null) {
            try {
                statisticsRange = StatisticsRange.valueOf(range.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid range, will use null (no timeline data)
            }
        }
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getSalesStatistics(statisticsRange)));
    }

    @GetMapping("/products/sales")
    public ResponseEntity<ApiResponse<ProductsSalesStatisticsResponse>> getProductsSalesStatistics() {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getProductsSalesStatistics()));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<InventoryStatisticsResponse>> getInventoryStatistics() {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getInventoryStatistics()));
    }
}
