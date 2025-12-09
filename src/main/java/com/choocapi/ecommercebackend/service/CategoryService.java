package com.choocapi.ecommercebackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.CategoryRequest;
import com.choocapi.ecommercebackend.dto.response.CategoryResponse;
import com.choocapi.ecommercebackend.entity.Category;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.CategoryMapper;
import com.choocapi.ecommercebackend.repository.CategoryRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }

        Category category = categoryMapper.toEntity(request);
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            category.setParent(parent);
        }
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    public Page<CategoryResponse> list(Pageable pageable, String search) {
        Specification<Category> specification = buildSpecification(search);
        return categoryRepository.findAll(specification, pageable).map(categoryMapper::toResponse);
    }

    public CategoryResponse get(Long id) {
        return categoryMapper.toResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public CategoryResponse getBySlug(String slug) {
        return categoryMapper.toResponse(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        categoryMapper.update(category, request);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            category.setParent(parent);
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private Specification<Category> buildSpecification(String search) {
        Specification<Category> specification = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("slug")), keyword)
            ));
        }

        return specification;
    }
}


