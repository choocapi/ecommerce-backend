package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import com.choocapi.ecommercebackend.dto.request.ProductRequest;
import com.choocapi.ecommercebackend.dto.response.ProductResponse;
import com.choocapi.ecommercebackend.entity.Product;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BrandMapper.class})
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "brandId", source = "brand.id")
    ProductResponse toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    void update(@MappingTarget Product product, ProductRequest request);

    default Page<ProductResponse> toPageResponse(Page<Product> products) {
        return products.map(this::toResponse);
    }
}
