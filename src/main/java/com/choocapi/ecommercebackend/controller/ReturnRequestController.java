package com.choocapi.ecommercebackend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.choocapi.ecommercebackend.dto.request.ReturnRequestRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.dto.response.ReturnRequestResponse;
import com.choocapi.ecommercebackend.service.ReturnRequestService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/return-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReturnRequestController {
    ReturnRequestService returnRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> createReturnRequest(
            @Valid @RequestBody ReturnRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(returnRequestService.createReturnRequest(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReturnRequestResponse>>> getAllReturnRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        Page<ReturnRequestResponse> result = returnRequestService.getReturnRequests(page, size, search, status);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<ReturnRequestResponse>>> getMyReturnRequests() {
        return ResponseEntity.ok(ApiResponse.success(returnRequestService.getMyReturnRequests()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> getReturnRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(returnRequestService.getReturnRequestById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> updateReturnRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(returnRequestService.updateReturnRequest(id, request)));
    }
}
