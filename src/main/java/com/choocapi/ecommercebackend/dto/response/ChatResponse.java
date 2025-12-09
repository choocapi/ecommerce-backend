package com.choocapi.ecommercebackend.dto.response;

import java.time.Instant;
import java.util.List;

import com.choocapi.ecommercebackend.dto.context.OrderSummary;
import com.choocapi.ecommercebackend.dto.context.ProductSummary;

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
public class ChatResponse {
    String reply;
    String queryType;
    List<ProductSummary> products;
    List<OrderSummary> orders;
    Instant timestamp;
}
