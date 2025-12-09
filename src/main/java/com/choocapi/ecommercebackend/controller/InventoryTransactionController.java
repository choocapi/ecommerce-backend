package com.choocapi.ecommercebackend.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.InventoryTransactionRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.InventoryTransactionResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.enums.InventoryTransactionType;
import com.choocapi.ecommercebackend.service.InventoryTransactionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/inventory-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionController {

    InventoryTransactionService service;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> create(@RequestBody InventoryTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransactionResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.list(pageable))));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransactionResponse>>> listByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) InventoryTransactionType type,
            Pageable pageable) {
        return ResponseEntity
                .ok(ApiResponse.success(PageResponse.from(service.listByProduct(productId, pageable, type))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Inventory transaction deleted successfully"));
    }
}

