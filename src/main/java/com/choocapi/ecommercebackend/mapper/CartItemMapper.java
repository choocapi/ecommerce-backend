package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.choocapi.ecommercebackend.dto.response.CartItemResponse;
import com.choocapi.ecommercebackend.entity.CartItem;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartItemMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "product", source = "product")
    CartItemResponse toResponse(CartItem entity);
}

