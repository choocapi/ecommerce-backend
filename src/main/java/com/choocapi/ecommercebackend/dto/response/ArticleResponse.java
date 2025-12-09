package com.choocapi.ecommercebackend.dto.response;

import java.time.Instant;

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
public class ArticleResponse {
    Long id;
    String title;
    String slug;
    String content;
    String featuredImage;
    String category;
    Boolean isPublished;
    String userId;
    UserResponse user;
    Instant publishedAt;
    Instant createdAt;
    Instant updatedAt;
}
