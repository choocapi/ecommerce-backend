package com.choocapi.ecommercebackend.service;

import com.choocapi.ecommercebackend.dto.request.RoleRequest;
import com.choocapi.ecommercebackend.dto.response.RoleResponse;
import com.choocapi.ecommercebackend.mapper.RoleMapper;
import com.choocapi.ecommercebackend.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository repository;
    RoleMapper mapper;

    public RoleResponse create(RoleRequest request) {
        var role = mapper.toEntity(request);

        role = repository.save(role);
        return mapper.toResponse(role);
    }

    public List<RoleResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public void delete(Long roleId) {
        repository.deleteById(roleId);
    }
}
