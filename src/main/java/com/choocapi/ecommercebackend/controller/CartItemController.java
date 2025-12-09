package com.choocapi.ecommercebackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.CartItemRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.CartItemResponse;
import com.choocapi.ecommercebackend.dto.response.CartSummaryResponse;
import com.choocapi.ecommercebackend.service.CartItemService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/cart-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartItemController {

    CartItemService service;

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> add(@RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.add(request)));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> batchAdd(@RequestBody List<CartItemRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.batchAdd(requests)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(service.list()));
    }

    @PatchMapping("/{id}/{quantity}")
    public ResponseEntity<ApiResponse<CartItemResponse>> update(@PathVariable Long id, @PathVariable Integer quantity) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, quantity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart item deleted successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(service.getSummary()));
    }
}

