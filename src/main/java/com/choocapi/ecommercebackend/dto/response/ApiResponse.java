package com.choocapi.ecommercebackend.dto.response;

import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    boolean succeeded;
    String message;
    T data;
    ApiError error;

    // Static factory method to create error response from ErrorCode
    public static ApiResponse<Object> error(ErrorCode errorCode) {
        return ApiResponse.builder()
                .succeeded(false)
                .error(ApiError.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .build();
    }

    // Static factory method to create success response with data
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .succeeded(true)
                .data(data)
                .build();
    }

    // Static factory method to create success response with data and message
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .succeeded(true)
                .message(message)
                .data(data)
                .build();
    }
}
