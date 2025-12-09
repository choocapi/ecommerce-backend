package com.choocapi.ecommercebackend.dto.context;

import java.math.BigDecimal;
import java.time.Instant;

import com.choocapi.ecommercebackend.enums.OrderStatus;

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
public class OrderSummary {
    String id;
    OrderStatus status;
    BigDecimal totalAmount;
    Instant orderedAt;
    Integer itemCount;
}
