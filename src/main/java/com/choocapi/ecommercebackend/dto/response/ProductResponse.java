package com.choocapi.ecommercebackend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String sku;
    String name;
    String slug;
    String description;
    String imageUrls;
    BigDecimal price;
    BigDecimal originalPrice;
    BigDecimal importPrice;
    Long categoryId;
    CategoryResponse category;
    Long brandId;
    BrandResponse brand;
    String specifications;
    Integer quantity;
    Integer reservedQuantity;
    Boolean isPublished;
    Instant publishedAt;
    Boolean isFeatured;
    Double averageRating;
    Long reviewCount;
}


