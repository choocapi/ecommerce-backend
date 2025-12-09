package com.choocapi.ecommercebackend.dto.response.statistics;

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
public class InventoryTransactionStatisticsResponse {
    Long id;
    String productName;
    String type;
    Integer quantity;
    BigDecimal price;
    String supplierName;
    Instant createdAt;
}
