package com.choocapi.ecommercebackend.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Structured output returned by the AI model when using Spring AI's
 * StructuredOutputConverter.
 *
 * This class represents what the model should return in JSON form:
 * - a natural language answer ("answer")
 * - a list of suggested product SKUs ("suggestedProductSkus")
 * - a list of suggested order IDs ("suggestedOrderIds")
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatAiResult {

    /**
     * Natural language reply that will be shown in the chatbot bubble.
     */
    String answer;

    /**
     * Optional list of product SKUs that the model suggests to highlight.
     * SKU được dùng vì nó xuất hiện rõ ràng trong ngữ cảnh gửi cho mô hình.
     */
    List<String> suggestedProductSkus;

    /**
     * Optional list of order IDs that the model suggests to highlight.
     */
    List<String> suggestedOrderIds;
}


