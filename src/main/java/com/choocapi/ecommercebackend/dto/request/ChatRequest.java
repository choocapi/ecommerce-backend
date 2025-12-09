package com.choocapi.ecommercebackend.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ChatRequest {
    @NotBlank(message = "Message cannot be blank")
    @Size(max = 1000, message = "Message too long")
    String message;

    // Optional: recent conversation history (last 3-4 messages)
    List<ChatMessagePayload> history;
}
