package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.choocapi.ecommercebackend.dto.request.UserCreationRequest;
import com.choocapi.ecommercebackend.dto.request.UserUpdateRequest;
import com.choocapi.ecommercebackend.dto.response.UserResponse;
import com.choocapi.ecommercebackend.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreationRequest request);

    UserResponse toResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSelf(@MappingTarget User user, UserUpdateRequest request);
}
