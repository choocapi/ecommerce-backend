package com.choocapi.ecommercebackend.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.context.OrderContext;
import com.choocapi.ecommercebackend.dto.context.OrderSummary;
import com.choocapi.ecommercebackend.dto.context.ProductContext;
import com.choocapi.ecommercebackend.dto.context.ProductSummary;
import com.choocapi.ecommercebackend.dto.context.StaticInfo;
import com.choocapi.ecommercebackend.dto.request.ChatMessagePayload;
import com.choocapi.ecommercebackend.dto.request.ChatRequest;
import com.choocapi.ecommercebackend.dto.response.ChatAiResult;
import com.choocapi.ecommercebackend.dto.response.ChatResponse;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.enums.QueryType;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatService {
    ProductContextProvider productContextProvider;
    OrderContextProvider orderContextProvider;
    StaticInfoProvider staticInfoProvider;
    ChatModel chatModel;
    UserRepository userRepository;

    // Static system instructions for the shopping assistant
    private static final String SYSTEM_PROMPT = String.join("\n",
            "Bạn là trợ lý mua sắm thông minh của ACB Computer Store.",
            "Nhiệm vụ của bạn:",
            "1. Giúp khách hàng tìm kiếm sản phẩm bằng cách trả lời các câu hỏi về thông số kỹ thuật, giá cả và tình trạng hàng.",
            "2. Cung cấp thông tin trạng thái và lịch sử đơn hàng cho người dùng đã đăng nhập.",
            "3. Trả lời các câu hỏi chung về công ty, chính sách và thông tin liên hệ.",
            "",
            "Hướng dẫn QUAN TRỌNG:",
            "- LUÔN LUÔN trả lời ngay với thông tin có sẵn, KHÔNG BAO GIỜ yêu cầu khách hàng lặp lại câu hỏi.",
            "- Nếu khách hỏi chung chung (ví dụ: 'tôi muốn mua laptop'), hãy giới thiệu NGAY các sản phẩm nổi bật có sẵn.",
            "- Trả lời ngắn gọn, thân thiện và bằng tiếng Việt.",
            "- Cung cấp thông tin chính xác dựa trên dữ liệu được cung cấp trong ngữ cảnh.",
            "- Với câu hỏi về đơn hàng, CHỈ thảo luận thông tin của người dùng đã xác thực.",
            "- Không bịa đặt thông tin sản phẩm hoặc giá cả.",
            "- Định dạng câu trả lời rõ ràng, dễ đọc, ưu tiên bullet points khi phù hợp.",
            "- Sử dụng đơn vị tiền tệ VNĐ cho giá cả, ví dụ: 15.000.000 VNĐ.",
            "- Khi giới thiệu sản phẩm, hãy đề cập tên, giá và 1–2 điểm nổi bật.",
            ""
    );

    /**
     * Get the current authenticated user from SecurityContext
     * Returns null if user is not authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String userId = authentication.getName();
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Determine the query type based on Vietnamese and English keywords
     */
    private QueryType determineQueryType(String message) {
        if (message == null || message.trim().isEmpty()) {
            return QueryType.UNKNOWN;
        }

        String lowerMessage = message.toLowerCase();

        // Product keywords (Vietnamese and English)
        List<String> productKeywords = Arrays.asList(
            "sản phẩm", "giá", "mua", "thông số", "cấu hình", "danh mục", "hãng", 
            "thương hiệu", "còn hàng", "có sẵn", "laptop", "pc", "máy tính", "linh kiện",
            "product", "price", "buy", "specification", "category", "brand", "stock", "available"
        );

        // Order keywords (Vietnamese and English)
        List<String> orderKeywords = Arrays.asList(
            "đơn hàng", "đơn", "mua hàng", "giao hàng", "vận chuyển", "trạng thái", 
            "theo dõi", "đơn của tôi", "đơn hàng của tôi",
            "order", "purchase", "delivery", "shipping", "status", "tracking", "my order"
        );

        // General keywords (Vietnamese and English)
        List<String> generalKeywords = Arrays.asList(
            "liên hệ", "giới thiệu", "chính sách", "phương thức thanh toán", "đổi trả", 
            "bảo hành", "hỗ trợ", "địa chỉ", "giờ làm việc",
            "contact", "about", "policy", "payment method", "return", "warranty", "support"
        );

        // Check for order keywords first (more specific)
        for (String keyword : orderKeywords) {
            if (lowerMessage.contains(keyword)) {
                return QueryType.ORDER;
            }
        }

        // Check for product keywords
        for (String keyword : productKeywords) {
            if (lowerMessage.contains(keyword)) {
                return QueryType.PRODUCT;
            }
        }

        // Check for general keywords
        for (String keyword : generalKeywords) {
            if (lowerMessage.contains(keyword)) {
                return QueryType.GENERAL;
            }
        }

        // Default to GENERAL for unclassified queries
        return QueryType.GENERAL;
    }

    /**
     * Build a Prompt for Spring AI ChatModel with system instructions, recent history and context.
     */
    private String buildPrompt(String message, QueryType queryType, User user,
            ProductContext productContext, OrderContext orderContext, StaticInfo staticInfo,
            List<ChatMessagePayload> history) {
        StringBuilder contextBuilder = new StringBuilder();

        switch (queryType) {
            case PRODUCT:
                ProductContext productData = productContext != null
                        ? productContext
                        : productContextProvider.getProductContext(message);
                contextBuilder.append("Danh sách sản phẩm (SKU|Tên|Giá VNĐ|Hãng|Danh mục|SL|Trạng thái):\n");
                if (productData.getProducts() != null && !productData.getProducts().isEmpty()) {
                    productData.getProducts().forEach(product -> {
                        contextBuilder.append(String.format("%s|%s|%,.0f|%s|%s|%d|%s\n",
                                product.getSku(),
                                product.getName(),
                                product.getPrice(),
                                product.getBrand() != null ? product.getBrand() : "N/A",
                                product.getCategory() != null ? product.getCategory() : "N/A",
                                product.getQuantity(),
                                Boolean.TRUE.equals(product.getIsPublished()) ? "Bán" : "Hết"
                        ));
                    });
                } else {
                    contextBuilder.append("Không tìm thấy sản phẩm.\n");
                }
                break;

            case ORDER:
                if (user == null) {
                    // User not authenticated - return early with login message
                    return "Vui lòng đăng nhập để xem thông tin đơn hàng của bạn.";
                }
                OrderContext orderData = orderContext != null
                        ? orderContext
                        : orderContextProvider.getOrderContext(user, message);
                contextBuilder.append("Danh sách đơn hàng (ID|Trạng thái|Tổng tiền VNĐ|Ngày đặt|SL sản phẩm):\n");
                if (orderData.getOrders() != null && !orderData.getOrders().isEmpty()) {
                    orderData.getOrders().forEach(order -> {
                        contextBuilder.append(String.format("%s|%s|%,.0f|%s|%d\n",
                                order.getId(),
                                order.getStatus(),
                                order.getTotalAmount(),
                                order.getOrderedAt(),
                                order.getItemCount()
                        ));
                    });
                } else {
                    contextBuilder.append("Chưa có đơn hàng.\n");
                }
                break;

            case GENERAL:
                StaticInfo staticData = staticInfo != null ? staticInfo : staticInfoProvider.getAllStaticInfo();
                contextBuilder.append("Thông tin cửa hàng:\n");
                contextBuilder.append(String.format("- Email: %s\n", staticData.getContactEmail()));
                contextBuilder.append(String.format("- Điện thoại: %s\n", staticData.getContactPhone()));
                contextBuilder.append(String.format("- Giờ làm việc: %s\n", staticData.getBusinessHours()));
                contextBuilder.append(String.format("- Địa chỉ: %s\n", staticData.getAddress()));
                contextBuilder.append(String.format("\nGiới thiệu: %s\n", staticData.getAboutUs()));
                contextBuilder.append(String.format("\n%s\n", staticData.getShippingPolicy()));
                contextBuilder.append(String.format("%s\n", staticData.getReturnPolicy()));
                contextBuilder.append(String.format("%s\n", staticData.getPrivacyPolicy()));
                contextBuilder.append("\nPhương thức thanh toán:\n");
                staticData.getPaymentMethods().forEach(method -> 
                    contextBuilder.append(String.format("- %s\n", method))
                );
                break;

            case UNKNOWN:
            default:
                // For unknown queries, provide general store info
                StaticInfo generalInfo = staticInfo != null ? staticInfo : staticInfoProvider.getAllStaticInfo();
                contextBuilder.append("Thông tin chung:\n");
                contextBuilder.append(String.format("- Email: %s\n", generalInfo.getContactEmail()));
                contextBuilder.append(String.format("- Điện thoại: %s\n", generalInfo.getContactPhone()));
                contextBuilder.append(String.format("- Giờ làm việc: %s\n", generalInfo.getBusinessHours()));
                break;
        }

        // Build user content: history + context + current question
        StringBuilder userContent = new StringBuilder();

        if (history != null && !history.isEmpty()) {
            String historySnippet = buildConversationSnippet(history);
            if (!historySnippet.isEmpty()) {
                userContent.append("Lịch sử hội thoại gần đây:\n");
                userContent.append(historySnippet).append("\n");
            }
        }

        if (contextBuilder.length() > 0) {
            userContent.append("Ngữ cảnh:\n");
            userContent.append(contextBuilder).append("\n");
        }

        userContent.append("Câu hỏi của khách hàng: ").append(message);

        // Combine system prompt and user content into a single text prompt
        return SYSTEM_PROMPT + "\n" + userContent;
    }

    private String buildConversationSnippet(List<ChatMessagePayload> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int maxMessages = 4;
        int start = Math.max(0, history.size() - maxMessages);

        for (int i = start; i < history.size(); i++) {
            ChatMessagePayload msg = history.get(i);
            if (msg.getText() == null || msg.getText().isBlank()) {
                continue;
            }
            String role = "user".equalsIgnoreCase(msg.getSender()) ? "Khách" : "Trợ lý";
            sb.append("- ").append(role).append(": ").append(msg.getText()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Call AI model with a text prompt, with error handling and fallback.
     */
    private String callAiModel(String prompt) {
        try {
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("Error calling AI model: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CHATBOT_AI_SERVICE_ERROR);
        }
    }

    /**
     * Process a user message and return a chatbot response
     * Orchestrates: classify query type, get context, build prompt, call AI, format response
     */
    public ChatResponse processMessage(ChatRequest request) {
        try {
            String message = request.getMessage();
            List<ChatMessagePayload> history = request.getHistory();

            // Get current user (null if not authenticated)
            User currentUser = getCurrentUser();

            // Determine query type
            QueryType queryType = determineQueryType(message);
            log.info("Query type determined: {} for message: {}", queryType, message);

            // Handle unauthenticated order queries
            if (queryType == QueryType.ORDER && currentUser == null) {
                return ChatResponse.builder()
                        .reply("Vui lòng đăng nhập để xem thông tin đơn hàng của bạn.")
                        .queryType(queryType.name())
                        .timestamp(Instant.now())
                        .build();
            }

            // Prepare context data based on query type
            ProductContext productContext = null;
            OrderContext orderContext = null;
            StaticInfo staticInfo = null;

            switch (queryType) {
                case PRODUCT:
                    productContext = productContextProvider.getProductContext(message);
                    break;
                case ORDER:
                    orderContext = orderContextProvider.getOrderContext(currentUser, message);
                    break;
                case GENERAL:
                case UNKNOWN:
                default:
                    staticInfo = staticInfoProvider.getAllStaticInfo();
                    break;
            }

            // Build base prompt with context and recent history
            String basePrompt = buildPrompt(message, queryType, currentUser, productContext, orderContext, staticInfo,
                    history);

            // Ask model to return structured JSON matching ChatAiResult using BeanOutputConverter
            BeanOutputConverter<ChatAiResult> outputConverter =
                    new BeanOutputConverter<>(ChatAiResult.class);

            String format = outputConverter.getFormat();
            String finalPrompt = basePrompt + "\n\n" + format;

            // Call AI model via Spring AI (string-based prompt)
            String rawResponse = callAiModel(finalPrompt);

            ChatAiResult aiResult = null;
            try {
                aiResult = outputConverter.convert(rawResponse);
            } catch (Exception e) {
                log.warn("Failed to convert structured AI output, falling back to raw text. Error: {}", e.getMessage());
            }

            String reply = (aiResult != null && aiResult.getAnswer() != null && !aiResult.getAnswer().isBlank())
                    ? aiResult.getAnswer()
                    : rawResponse;

            // Let AI decide which products/orders to show, but always map IDs back to DB data
            List<ProductSummary> suggestedProducts = selectSuggestedProducts(productContext, aiResult);
            List<com.choocapi.ecommercebackend.dto.context.OrderSummary> suggestedOrders =
                    selectSuggestedOrders(orderContext, aiResult);

            return ChatResponse.builder()
                    .reply(reply)
                    .queryType(queryType.name())
                    .products(suggestedProducts)
                    .orders(suggestedOrders)
                    .timestamp(Instant.now())
                    .build();

        } catch (AppException e) {
            // Re-throw AppException (already has proper error code)
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing message: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CHATBOT_CONTEXT_ERROR);
        }
    }

    private List<ProductSummary> selectSuggestedProducts(ProductContext productContext, ChatAiResult aiResult) {
        if (productContext == null || productContext.getProducts() == null || productContext.getProducts().isEmpty()) {
            return null;
        }

        List<ProductSummary> all = productContext.getProducts();

        if (aiResult == null || aiResult.getSuggestedProductSkus() == null
                || aiResult.getSuggestedProductSkus().isEmpty()) {
            // Fallback: return top 5 products from context
            return all.stream().limit(5).collect(Collectors.toList());
        }

        Set<String> skuSet = aiResult.getSuggestedProductSkus().stream()
                .filter(sku -> sku != null && !sku.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<ProductSummary> selected = all.stream()
                .filter(p -> p.getSku() != null && skuSet.contains(p.getSku().toLowerCase()))
                .limit(8) // increased limit for better suggestions
                .collect(Collectors.toList());

        // If AI returned SKUs không khớp context, fallback
        if (selected.isEmpty()) {
            return all.stream().limit(5).collect(Collectors.toList());
        }

        return selected;
    }

    private List<OrderSummary> selectSuggestedOrders(
            OrderContext orderContext,
            ChatAiResult aiResult) {
        if (orderContext == null || orderContext.getOrders() == null || orderContext.getOrders().isEmpty()) {
            return null;
        }

        List<OrderSummary> all = orderContext.getOrders();

        if (aiResult == null || aiResult.getSuggestedOrderIds() == null || aiResult.getSuggestedOrderIds().isEmpty()) {
            // Fallback: recent 5 orders
            return all.stream().limit(5).collect(Collectors.toList());
        }

        Set<String> idSet = aiResult.getSuggestedOrderIds().stream()
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        List<com.choocapi.ecommercebackend.dto.context.OrderSummary> selected = all.stream()
                .filter(o -> o.getId() != null && idSet.contains(o.getId()))
                .limit(8)
                .collect(Collectors.toList());

        if (selected.isEmpty()) {
            return all.stream().limit(5).collect(Collectors.toList());
        }

        return selected;
    }
}
