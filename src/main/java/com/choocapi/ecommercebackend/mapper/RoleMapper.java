package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.choocapi.ecommercebackend.dto.request.RoleRequest;
import com.choocapi.ecommercebackend.dto.response.RoleResponse;
import com.choocapi.ecommercebackend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "id", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);
}
