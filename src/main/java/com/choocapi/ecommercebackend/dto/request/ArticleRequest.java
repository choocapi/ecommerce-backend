package com.choocapi.ecommercebackend.dto.request;

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
public class ArticleRequest {
    String title;
    String slug;
    String content;
    String featuredImage;
    String category;
    Boolean isPublished;
    Instant publishedAt;
}
