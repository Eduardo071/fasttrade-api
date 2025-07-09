package com.fasttrade.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatusEnum {
    PENDING("Pendente"),
    MATCHED("Combinada"),
    COMPLETED("Concluída"),
    CANCELLED("Cancelada");

    private final String description;
}
