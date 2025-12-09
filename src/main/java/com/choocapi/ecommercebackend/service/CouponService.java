package com.choocapi.ecommercebackend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.request.CouponRequest;
import com.choocapi.ecommercebackend.dto.response.CouponResponse;
import com.choocapi.ecommercebackend.entity.Coupon;
import com.choocapi.ecommercebackend.enums.CouponType;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.CouponMapper;
import com.choocapi.ecommercebackend.repository.CouponRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponService {
    CouponRepository repository;
    CouponMapper mapper;

    public CouponResponse create(CouponRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }
        validateDates(request.getStartDate(), request.getEndDate());
        Coupon entity = mapper.toEntity(request);
        entity.setUsedCount(0);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public Page<CouponResponse> list(Pageable pageable, String search, String type, Boolean isActive) {
        Specification<Coupon> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("code")), keyword));
            }
            if (type != null) {
                try {
                    CouponType couponType = CouponType.valueOf(type.toUpperCase());
                    predicates.add(cb.equal(root.get("type"), couponType));
                } catch (IllegalArgumentException e) {
                    predicates.add(cb.disjunction());
                }
            }
            if (isActive != null) predicates.add(cb.equal(root.get("isActive"), isActive));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    public CouponResponse get(Long id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public CouponResponse getByCode(String code) {
        return mapper.toResponse(repository.findByCode(code).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public CouponResponse update(Long id, CouponRequest request) {
        Coupon entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (request.getCode() != null && !request.getCode().equals(entity.getCode()) && repository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }
        if (request.getStartDate() != null || request.getEndDate() != null) {
            validateDates(request.getStartDate() != null ? request.getStartDate() : entity.getStartDate(),
                    request.getEndDate() != null ? request.getEndDate() : entity.getEndDate());
        }
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void validateDates(Instant start, Instant end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
    }
}


