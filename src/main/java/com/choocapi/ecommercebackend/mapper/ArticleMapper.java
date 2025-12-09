package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.choocapi.ecommercebackend.dto.request.ArticleRequest;
import com.choocapi.ecommercebackend.dto.response.ArticleResponse;
import com.choocapi.ecommercebackend.entity.Article;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ArticleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Article toEntity(ArticleRequest request);

    @Mapping(target = "userId", source = "user.id")
    ArticleResponse toResponse(Article article);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Article article, ArticleRequest request);
}
