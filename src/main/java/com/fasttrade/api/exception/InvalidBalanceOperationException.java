package com.fasttrade.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidBalanceOperationException extends RuntimeException {
    public InvalidBalanceOperationException() {
        super("Operação de saldo inválida.");
    }
}
