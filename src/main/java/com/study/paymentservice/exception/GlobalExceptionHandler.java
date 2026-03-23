package com.study.paymentservice.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

public class GlobalExceptionHandler {

    @ExceptionHandler(RandomNumberClientException.class)
    public ResponseEntity<?> handleNoConnectionWithRandomNumberApi(RandomNumberClientException ex) {
        String message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error(HttpStatus.FORBIDDEN, message));
    }

    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<?> handleNoParamsGetPayments(PaymentServiceException ex) {
        String message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, message));
    }

    private Map<String, Object> error(HttpStatus status, String message) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", message
        );
    }

    private Map<String, Object> error(HttpStatus status, String message, String key, Object value) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", message,
                key, value
        );
    }
}
