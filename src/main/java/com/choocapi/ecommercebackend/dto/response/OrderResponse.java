package com.choocapi.ecommercebackend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
public class OrderResponse {
    String id;
    String customerId;
    UserResponse user;
    String status;
    String shippingName;
    String shippingPhone;
    String shippingAddress;
    String shippingWard;
    String shippingDistrict;
    String shippingCity;
    String paymentMethod;
    String paymentStatus;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String couponCode;
    Instant orderedAt;
    Instant confirmedAt;
    Instant shippedAt;
    Instant deliveredAt;
    Instant cancelledAt;
    List<OrderItemResponse> items;
}

