package com.choocapi.ecommercebackend.dto.response.statistics;

import java.math.BigDecimal;
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
public class InventoryStatisticsResponse {
    Long totalProducts;
    Long publishedProducts;
    Long lowStockProducts;
    BigDecimal totalInventoryValue;
    List<InventoryTransactionStatisticsResponse> recentTransactions;
}
