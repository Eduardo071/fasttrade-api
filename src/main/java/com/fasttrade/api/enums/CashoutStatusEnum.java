package com.fasttrade.api.enums;

import lombok.Getter;

@Getter
public enum CashoutStatusEnum {
    PENDING("Em andamento"),
    FAILED("Falha"),
    SUCCESS("Sucesso");

    private final String description;

    CashoutStatusEnum(String description) {
        this.description = description;
    }
}
