package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.choocapi.ecommercebackend.dto.request.CouponRequest;
import com.choocapi.ecommercebackend.dto.response.CouponResponse;
import com.choocapi.ecommercebackend.entity.Coupon;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "type", expression = "java(request.getType() != null ? com.choocapi.ecommercebackend.enums.CouponType.valueOf(request.getType().toUpperCase()) : null)")
    Coupon toEntity(CouponRequest request);

    @Mapping(target = "type", expression = "java(coupon.getType() != null ? coupon.getType().name() : null)")
    CouponResponse toResponse(Coupon coupon);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "type", expression = "java(request.getType() != null ? com.choocapi.ecommercebackend.enums.CouponType.valueOf(request.getType().toUpperCase()) : coupon.getType())")
    void update(@MappingTarget Coupon coupon, CouponRequest request);
}


