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

import com.choocapi.ecommercebackend.dto.request.UserAddressRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.UserAddressResponse;
import com.choocapi.ecommercebackend.service.UserAddressService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/users/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAddressController {

    UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAddressResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(userAddressService.listMyAddresses()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressResponse>> create(@RequestBody UserAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userAddressService.createAddress(request)));
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<ApiResponse<UserAddressResponse>> update(@PathVariable Long addressId, @RequestBody UserAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userAddressService.updateAddress(addressId, request)));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long addressId) {
        userAddressService.deleteAddress(addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }
}


