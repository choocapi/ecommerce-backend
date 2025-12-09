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

import com.choocapi.ecommercebackend.dto.request.ArticleRequest;
import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import com.choocapi.ecommercebackend.dto.response.ArticleResponse;
import com.choocapi.ecommercebackend.dto.response.PageResponse;
import com.choocapi.ecommercebackend.service.ArticleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleController {

    ArticleService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> list(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.list(pageable, search, isPublished, category))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(service.getBySlug(slug)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> update(
            @PathVariable Long id, @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Article deleted successfully"));
    }
}







