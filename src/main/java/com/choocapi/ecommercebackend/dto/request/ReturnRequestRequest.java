package com.choocapi.ecommercebackend.dto.request;

import com.choocapi.ecommercebackend.enums.ReturnStatus;

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
public class ReturnRequestRequest {
    String orderId;
    String reason;
    String imageUrls;
    ReturnStatus status;
    String adminNote;
}
