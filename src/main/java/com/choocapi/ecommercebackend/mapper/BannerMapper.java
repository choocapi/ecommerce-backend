package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.*;

import com.choocapi.ecommercebackend.dto.request.BannerRequest;
import com.choocapi.ecommercebackend.dto.response.BannerResponse;
import com.choocapi.ecommercebackend.entity.Banner;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    @Mapping(target = "id", ignore = true)
    Banner toEntity(BannerRequest request);

    BannerResponse toResponse(Banner banner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Banner banner, BannerRequest request);
}


