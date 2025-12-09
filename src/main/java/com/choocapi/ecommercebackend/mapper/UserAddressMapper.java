package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.choocapi.ecommercebackend.dto.request.UserAddressRequest;
import com.choocapi.ecommercebackend.dto.response.UserAddressResponse;
import com.choocapi.ecommercebackend.entity.UserAddress;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserAddress toEntity(UserAddressRequest request);
    UserAddressResponse toResponse(UserAddress address);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    void update(@MappingTarget UserAddress target, UserAddressRequest request);
}


