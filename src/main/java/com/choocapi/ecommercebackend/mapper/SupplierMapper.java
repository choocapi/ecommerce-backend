package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.choocapi.ecommercebackend.dto.request.SupplierRequest;
import com.choocapi.ecommercebackend.dto.response.SupplierResponse;
import com.choocapi.ecommercebackend.entity.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    @Mapping(target = "id", ignore = true)
    Supplier toEntity(SupplierRequest request);

    SupplierResponse toResponse(Supplier supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Supplier supplier, SupplierRequest request);
}

