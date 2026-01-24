package com.example.RestaurantApplication.module.user.utils.Exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.RestaurantApplication.module.user.utils.ResponseWrapper.ApiResponse;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        ApiResponse<?> response = ApiResponse.error(
            ex.getMessage(),
            ex.getCode(),
            null    
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    // Handle Body Missing or Malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingBody() {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Request body is missing or invalid JSON", "400", null));
    }


    // Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation error");
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorMessage, "400", null));
    }

    // Handle HTTP Method Not Supported
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {
        String message = "Method " + ex.getMethod() + " is not supported for this endpoint";

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(
                        message,
                        "405",
                        null
                ));
    }
}
