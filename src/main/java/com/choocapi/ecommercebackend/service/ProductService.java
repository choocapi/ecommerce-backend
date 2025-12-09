package com.choocapi.ecommercebackend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.ProductRequest;
import com.choocapi.ecommercebackend.dto.response.ProductResponse;
import com.choocapi.ecommercebackend.entity.Brand;
import com.choocapi.ecommercebackend.entity.Category;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.ProductMapper;
import com.choocapi.ecommercebackend.repository.BrandRepository;
import com.choocapi.ecommercebackend.repository.CategoryRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.ProductReviewRepository;

import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository repository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductReviewRepository productReviewRepository;
    ProductMapper mapper;

    public ProductResponse create(ProductRequest request) {
        if (repository.existsBySku(request.getSku()) || repository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }

        Product entity = mapper.toEntity(request);
        normalizePricing(entity);
        attachRelations(entity, request);
        entity = repository.save(entity);
        return mapWithReviewStats(entity);
    }

    public Page<ProductResponse> list(Pageable pageable, String search, Long categoryId, Long brandId) {
        Specification<Product> specification = buildSpecification(search, categoryId, brandId);
        return repository.findAll(specification, pageable)
                .map(this::mapWithReviewStats);
    }

    public ProductResponse get(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return mapWithReviewStats(product);
    }

    public ProductResponse getBySlug(String slug) {
        Product product = repository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return mapWithReviewStats(product);
    }

    public Page<ProductResponse> getByCategorySlug(String categorySlug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        // Get all categories including the parent and all its descendants recursively
        Set<Category> allCategories = new HashSet<>();
        allCategories.add(category);
        findAllDescendants(category, allCategories);
        
        return repository.findByCategories(new ArrayList<>(allCategories), pageable)
                .map(this::mapWithReviewStats);
    }
    
    private void findAllDescendants(Category category, Set<Category> result) {
        List<Category> children = categoryRepository.findByParent(category);
        for (Category child : children) {
            result.add(child);
            findAllDescendants(child, result); // Recursive call for nested children
        }
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        mapper.update(entity, request);
        normalizePricing(entity);
        attachRelations(entity, request);
        return mapWithReviewStats(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void attachRelations(Product entity, ProductRequest request) {
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            entity.setCategory(category);
        }
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            entity.setBrand(brand);
        }
    }

    private void normalizePricing(Product entity) {
        if (entity.getOriginalPrice() == null || entity.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            entity.setOriginalPrice(entity.getPrice());
        }
        if (entity.getPrice() == null && entity.getOriginalPrice() != null) {
            entity.setPrice(entity.getOriginalPrice());
        }
    }

    private Specification<Product> buildSpecification(String search, Long categoryId, Long brandId) {
        Specification<Product> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("sku")), keyword)
            ));
        }

        if (categoryId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("category", JoinType.LEFT).get("id"), categoryId));
        }

        if (brandId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join("brand", JoinType.LEFT).get("id"), brandId));
        }

        return specification;
    }

    private ProductResponse mapWithReviewStats(Product product) {
        ProductResponse response = mapper.toResponse(product);
        Double averageRating = productReviewRepository.findAverageRatingByProduct(product);
        Long reviewCount = productReviewRepository.countVisibleReviewsByProduct(product);
        response.setAverageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0d);
        response.setReviewCount(reviewCount != null ? reviewCount : 0L);
        return response;
    }
}


