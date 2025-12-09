package com.choocapi.ecommercebackend.dto.request;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String sku;
    String name;
    String slug;
    String description;
    String imageUrls;
    BigDecimal price;
    BigDecimal originalPrice;
    BigDecimal importPrice;
    String specifications;
    Boolean isPublished;
    Boolean isFeatured;
    Integer quantity;
    Integer reservedQuantity;
    Long categoryId;
    Long brandId;
}
