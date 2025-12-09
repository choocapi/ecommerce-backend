package com.choocapi.ecommercebackend.controller;

import com.choocapi.ecommercebackend.dto.request.RoleRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.RoleResponse;
import com.choocapi.ecommercebackend.service.RoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService service;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@RequestBody @Valid RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list() {
        return ResponseEntity.ok()
                .body(ApiResponse.success(service.list()));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long roleId) {
        service.delete(roleId);
        return ResponseEntity.ok()
                .body(ApiResponse.success(null, "Role deleted successfully"));
    }
}
