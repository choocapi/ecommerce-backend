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

import com.choocapi.ecommercebackend.dto.request.ChangePasswordRequest;
import com.choocapi.ecommercebackend.dto.request.UserCreationRequest;
import com.choocapi.ecommercebackend.dto.request.UserUpdateRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.dto.response.UserResponse;
import com.choocapi.ecommercebackend.service.UserService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService service;

    @PostMapping()
    public ResponseEntity<ApiResponse<UserResponse>> create(@RequestBody @Valid UserCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String excludeRole,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Boolean verified) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(PageResponse.from(service.list(pageable, search, role, excludeRole, status, verified))));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable String userId) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.get(userId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.getMyInfo()));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.update(userId, request)));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(@RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.changePassword(request)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.updateMyProfile(request)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String userId) {
        service.delete(userId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(null, "Delete user successfully"));
    }
}
