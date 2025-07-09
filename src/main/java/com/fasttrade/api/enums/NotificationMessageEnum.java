package com.fasttrade.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationMessageEnum {

    MATCH_FOUND_TITLE("🔁 Match encontrado!"),
    MATCH_FOUND_BODY("Sua intenção de troca foi correspondida com sucesso."),

    TRADE_COMPLETED_TITLE("✅ Troca realizada!"),
    TRADE_COMPLETED_BODY("A transação de câmbio foi finalizada com sucesso. Confira seu saldo!"),

    ERROR_TITLE("⚠️ Algo deu errado!"),
    ERROR_BODY("Ocorreu um erro ao processar sua operação.");

    private final String value;
}
