package com.choocapi.ecommercebackend.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.SupplierRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.dto.response.SupplierResponse;
import com.choocapi.ecommercebackend.service.SupplierService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplierController {

    SupplierService service;

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SupplierResponse>>> list(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.list(pageable, search))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(@PathVariable Long id, @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Supplier deleted successfully"));
    }
}

