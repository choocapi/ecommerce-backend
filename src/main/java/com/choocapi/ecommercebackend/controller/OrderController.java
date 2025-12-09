package com.choocapi.ecommercebackend.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.ApplyCouponRequest;
import com.choocapi.ecommercebackend.dto.request.OrderRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.OrderResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.service.OrderService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService service;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request)));
    }

    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createFromCart(@RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createFromCart(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> list(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.list(pageable, search, status, paymentMethod))));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        return ResponseEntity.ok(ApiResponse.success(service.getMyOrders()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(@PathVariable String id, @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateOrder(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Order deleted successfully"));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.confirmOrder(id)));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> ship(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.shipOrder(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancelOrder(id)));
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmDelivery(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.confirmDelivery(id)));
    }

    @PostMapping("/{id}/apply-coupon")
    public ResponseEntity<ApiResponse<OrderResponse>> applyCoupon(@PathVariable String id, @RequestBody ApplyCouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.applyCoupon(id, request.getCode())));
    }
}

