package com.choocapi.ecommercebackend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.request.BannerRequest;
import com.choocapi.ecommercebackend.dto.response.BannerResponse;
import com.choocapi.ecommercebackend.entity.Banner;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.BannerMapper;
import com.choocapi.ecommercebackend.repository.BannerRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BannerService {
    BannerRepository repository;
    BannerMapper mapper;

    public BannerResponse create(BannerRequest request) {
        Banner entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public Page<BannerResponse> list(Pageable pageable) {
        Specification<Banner> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    public BannerResponse get(Long id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public BannerResponse update(Long id, BannerRequest request) {
        Banner entity = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}


