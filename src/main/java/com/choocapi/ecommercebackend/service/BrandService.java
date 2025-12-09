package com.choocapi.ecommercebackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.BrandRequest;
import com.choocapi.ecommercebackend.dto.response.BrandResponse;
import com.choocapi.ecommercebackend.entity.Brand;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.BrandMapper;
import com.choocapi.ecommercebackend.repository.BrandRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {
    BrandRepository brandRepository;
    BrandMapper brandMapper;

    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }
        Brand brand = brandMapper.toEntity(request);
        brand = brandRepository.save(brand);
        return brandMapper.toResponse(brand);
    }

    public Page<BrandResponse> list(Pageable pageable, String search) {
        Specification<Brand> specification = buildSpecification(search);
        return brandRepository.findAll(specification, pageable).map(brandMapper::toResponse);
    }

    public BrandResponse get(Long id) {
        return brandMapper.toResponse(brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        brandMapper.update(brand, request);
        return brandMapper.toResponse(brandRepository.save(brand));
    }

    public void delete(Long id) {
        brandRepository.deleteById(id);
    }

    private Specification<Brand> buildSpecification(String search) {
        Specification<Brand> specification = (root, query, cb) -> cb.conjunction();

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


