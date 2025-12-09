package com.choocapi.ecommercebackend.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.request.ProductReviewRequest;
import com.choocapi.ecommercebackend.dto.response.ProductReviewResponse;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.ProductReview;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.ProductReviewMapper;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.ProductReviewRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductReviewService {
    ProductReviewRepository repository;
    ProductRepository productRepository;
    UserRepository userRepository;
    ProductReviewMapper mapper;

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    public ProductReviewResponse create(ProductReviewRequest request) {
        User user = getCurrentUser();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        ProductReview entity = mapper.toEntity(request);
        entity.setProduct(product);
        entity.setUser(user);
        entity.setIsHidden(false);

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public List<ProductReviewResponse> listByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return repository.findByProductAndIsHiddenFalseOrderByCreatedAtDesc(product)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductReviewResponse> getMyReviews() {
        User currentUser = getCurrentUser();
        return repository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void delete(Long id) {
        ProductReview review = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User currentUser = getCurrentUser();
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        repository.deleteById(id);
    }
}
