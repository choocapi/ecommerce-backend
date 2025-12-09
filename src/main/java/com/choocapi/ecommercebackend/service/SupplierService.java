package com.choocapi.ecommercebackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.SupplierRequest;
import com.choocapi.ecommercebackend.dto.response.SupplierResponse;
import com.choocapi.ecommercebackend.entity.Supplier;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.SupplierMapper;
import com.choocapi.ecommercebackend.repository.SupplierRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplierService {
    SupplierRepository repository;
    SupplierMapper mapper;

    public SupplierResponse create(SupplierRequest request) {
        Supplier entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public Page<SupplierResponse> list(Pageable pageable, String search) {
        Specification<Supplier> specification = buildSpecification(search);
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    public SupplierResponse get(Long id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private Specification<Supplier> buildSpecification(String search) {
        Specification<Supplier> specification = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("contactPerson")), keyword),
                    cb.like(cb.lower(root.get("email")), keyword),
                    cb.like(cb.lower(root.get("phoneNumber")), keyword),
                    cb.like(cb.lower(root.get("address")), keyword)
            ));
        }

        return specification;
    }
}

