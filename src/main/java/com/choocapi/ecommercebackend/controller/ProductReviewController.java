package com.choocapi.ecommercebackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.ProductReviewRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.ProductReviewResponse;
import com.choocapi.ecommercebackend.service.ProductReviewService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/product-reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductReviewController {

    ProductReviewService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductReviewResponse>> create(
            @Valid @RequestBody ProductReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductReviewResponse>>> listByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByProduct(productId)));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<List<ProductReviewResponse>>> getMyReviews() {
        return ResponseEntity.ok(ApiResponse.success(service.getMyReviews()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }
}
