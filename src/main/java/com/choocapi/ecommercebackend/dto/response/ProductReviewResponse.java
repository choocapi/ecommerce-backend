package com.choocapi.ecommercebackend.dto.response;

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
public class ProductReviewResponse {
    Long id;
    Long productId;
    ProductResponse product;
    String userId;
    UserResponse user;
    Integer rating;
    String content;
    String imageUrls; // JSON string array: ["url1", "url2", ...]
    Boolean isPurchased;
    Boolean isHidden;
    Instant createdAt;
}
