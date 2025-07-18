package com.fasttrade.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FirebaseProcessingException extends RuntimeException {
    public FirebaseProcessingException(String message) {
        super(message);
    }
}

