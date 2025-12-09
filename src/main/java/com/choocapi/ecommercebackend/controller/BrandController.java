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

import com.choocapi.ecommercebackend.dto.request.BrandRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.BrandResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.service.BrandService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandController {

    BrandService brandService;

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> create(@RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(brandService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BrandResponse>>> list(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(brandService.list(pageable, search))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.get(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> update(@PathVariable Long id, @RequestBody BrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brandService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Brand deleted successfully"));
    }
}


