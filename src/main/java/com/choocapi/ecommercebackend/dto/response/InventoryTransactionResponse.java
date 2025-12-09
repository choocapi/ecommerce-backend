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
public class InventoryTransactionResponse {
    Long id;
    Long productId;
    ProductResponse product;
    Long supplierId;
    SupplierResponse supplier;
    String orderId;
    String type;
    Integer quantity;
    BigDecimal price;
    Integer previousQuantity;
    Integer resultingQuantity;
    String note;
    Instant createdAt;
}

