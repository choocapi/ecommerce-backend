package com.choocapi.ecommercebackend.dto.request;

import java.math.BigDecimal;
import java.util.List;

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
public class OrderRequest {
    String customerId;
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
    List<OrderItemRequest> items;
}

