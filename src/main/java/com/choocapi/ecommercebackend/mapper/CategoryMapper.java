package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.choocapi.ecommercebackend.dto.request.CategoryRequest;
import com.choocapi.ecommercebackend.dto.response.CategoryResponse;
import com.choocapi.ecommercebackend.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    void update(@MappingTarget Category category, CategoryRequest request);
}


