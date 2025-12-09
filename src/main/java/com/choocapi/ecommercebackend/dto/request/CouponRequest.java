package com.choocapi.ecommercebackend.dto.request;

import java.math.BigDecimal;
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
public class CouponRequest {
    String code;
    String description;
    String type;
    BigDecimal value;
    Integer usageLimit;
    Instant startDate;
    Instant endDate;
    Boolean isActive;
}


