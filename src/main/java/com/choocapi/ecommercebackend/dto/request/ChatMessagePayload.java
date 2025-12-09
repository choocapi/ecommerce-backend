package com.choocapi.ecommercebackend.dto.request;

import java.time.Instant;
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
public class ChatMessagePayload {
    String sender; // "user" | "bot"
    String text;
    String queryType;
    List<Long> productIds;
    List<String> orderIds;
    Instant timestamp;
}


