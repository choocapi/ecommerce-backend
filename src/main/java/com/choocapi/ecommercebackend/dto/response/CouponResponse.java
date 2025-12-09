package com.choocapi.ecommercebackend.dto.response;

import java.math.BigDecimal;
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
public class CouponResponse {
    Long id;
    String code;
    String description;
    String type;
    BigDecimal value;
    Integer usageLimit;
    Integer usedCount;
    Instant startDate;
    Instant endDate;
    Boolean isActive;
}


