package com.choocapi.ecommercebackend.dto.response;

import java.time.Instant;

import com.choocapi.ecommercebackend.enums.ReturnStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReturnRequestResponse {
    Long id;
    String orderId;
    String userId;
    UserResponse user;
    String reason;
    String imageUrls;
    ReturnStatus status;
    String adminNote;
    Instant createdAt;
    Instant approvedAt;
    Instant rejectedAt;
    Instant completedAt;
}
