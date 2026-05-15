package com.study.paymentservice.exception;

public class RandomNumberClientException extends RuntimeException{
    public RandomNumberClientException(String message) {
        super(message);
    }

    public RandomNumberClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
