package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.choocapi.ecommercebackend.dto.request.ProductReviewRequest;
import com.choocapi.ecommercebackend.dto.response.ProductReviewResponse;
import com.choocapi.ecommercebackend.entity.ProductReview;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, UserMapper.class})
public interface ProductReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isHidden", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductReview toEntity(ProductReviewRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "userId", source = "user.id")
    ProductReviewResponse toResponse(ProductReview entity);
}
