package com.choocapi.ecommercebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // General errors
    UNCATEGORIZED_EXCEPTION("UNCATEGORIZED_EXCEPTION", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY("INVALID_KEY", "Invalid key provided", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found", HttpStatus.NOT_FOUND),
    
    // Authentication & Authorization errors
    UNAUTHENTICATED("UNAUTHENTICATED", "Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("UNAUTHORIZED", "Access denied - insufficient permissions", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("INVALID_TOKEN", "Authentication token is invalid", HttpStatus.UNAUTHORIZED),
    MISSING_TOKEN("MISSING_TOKEN", "Authentication token is missing", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("TOKEN_INVALID", "Token is invalid", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token has expired", HttpStatus.BAD_REQUEST),
    
    // User management errors
    USER_EXISTED("USER_EXISTED", "User with this email already exists", HttpStatus.CONFLICT),
    USER_NOT_EXIST("USER_NOT_EXIST", "User does not exist", HttpStatus.NOT_FOUND),
    USER_NOT_VERIFIED("USER_NOT_VERIFIED", "User email is not verified", HttpStatus.FORBIDDEN),
    USER_INACTIVE("USER_INACTIVE", "User account is inactive", HttpStatus.FORBIDDEN),
    USER_LOCKED("USER_LOCKED", "User account is locked", HttpStatus.FORBIDDEN),
    
    // Role & Permission errors
    ROLE_NOT_EXIST("ROLE_NOT_EXIST", "Role does not exist", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("ROLE_ALREADY_EXISTS", "Role already exists", HttpStatus.CONFLICT),
    PERMISSION_NOT_EXIST("PERMISSION_NOT_EXIST", "Permission does not exist", HttpStatus.NOT_FOUND),
    PERMISSION_ALREADY_EXISTS("PERMISSION_ALREADY_EXISTS", "Permission already exists", HttpStatus.CONFLICT),
    
    // Business logic errors
    DUPLICATE_ENTRY("DUPLICATE_ENTRY", "Duplicate entry detected", HttpStatus.CONFLICT),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request data", HttpStatus.BAD_REQUEST),
    
    // Inventory management errors
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "Insufficient stock quantity", HttpStatus.BAD_REQUEST),
    
    // Rate limiting & Security
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    
    // Data validation
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", "Required field is missing", HttpStatus.BAD_REQUEST),
    
    // Chatbot errors
    CHATBOT_AI_SERVICE_ERROR("CHATBOT_AI_SERVICE_ERROR", "Dịch vụ AI tạm thời không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    CHATBOT_CONTEXT_ERROR("CHATBOT_CONTEXT_ERROR", "Không thể truy xuất thông tin", HttpStatus.INTERNAL_SERVER_ERROR),
    CHATBOT_LOGIN_REQUIRED("CHATBOT_LOGIN_REQUIRED", "Vui lòng đăng nhập để xem thông tin đơn hàng", HttpStatus.UNAUTHORIZED),
    
    // Return request errors
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_DELIVERED("ORDER_NOT_DELIVERED", "Order must be delivered before requesting return", HttpStatus.BAD_REQUEST),
    RETURN_REQUEST_ALREADY_EXISTS("RETURN_REQUEST_ALREADY_EXISTS", "Return request already exists for this order", HttpStatus.CONFLICT),
    RETURN_REQUEST_NOT_FOUND("RETURN_REQUEST_NOT_FOUND", "Return request not found", HttpStatus.NOT_FOUND),
    INVALID_RETURN_STATUS("INVALID_RETURN_STATUS", "Invalid return status transition", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(String code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
