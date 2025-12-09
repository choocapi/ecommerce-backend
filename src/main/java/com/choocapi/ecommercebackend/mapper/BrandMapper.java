package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.choocapi.ecommercebackend.dto.request.BrandRequest;
import com.choocapi.ecommercebackend.dto.response.BrandResponse;
import com.choocapi.ecommercebackend.entity.Brand;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    @Mapping(target = "id", ignore = true)
    Brand toEntity(BrandRequest request);

    BrandResponse toResponse(Brand brand);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Brand brand, BrandRequest request);
}


