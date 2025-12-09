package com.choocapi.ecommercebackend.dto.request;

import java.math.BigDecimal;

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
public class InventoryTransactionRequest {
    Long productId;
    Long supplierId;
    String type;
    Integer quantity;
    Integer targetQuantity;
    BigDecimal price;
    String orderId;
    String note;
}

