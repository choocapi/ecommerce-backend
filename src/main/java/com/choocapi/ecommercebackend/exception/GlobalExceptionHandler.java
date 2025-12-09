package com.choocapi.ecommercebackend.exception;

import com.choocapi.ecommercebackend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Helper method to build error response from ErrorCode
    private ResponseEntity<ApiResponse> buildErrorResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handlingUncategorizedException(Exception exception) {
        return buildErrorResponse(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        return buildErrorResponse(exception.getErrorCode());
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handlingAuthorizationDeniedException(AuthorizationDeniedException exception) {
        return buildErrorResponse(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingValidationException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            System.err.println("Error key not found in ErrorCode enum: " + enumKey);
        }

        return buildErrorResponse(errorCode);
    }
}
