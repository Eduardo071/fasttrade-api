package com.fasttrade.api.constant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultWalletConstants {

    public static final Map<String, BigDecimal> DEFAULT_BALANCES;
    public static final String DATE_TIME_NOW = LocalDateTime.now().toString();

    static {
        DEFAULT_BALANCES = new LinkedHashMap<>();
        DEFAULT_BALANCES.put("USD", BigDecimal.ZERO);
        DEFAULT_BALANCES.put("BRL", BigDecimal.ZERO);
        DEFAULT_BALANCES.put("EUR", BigDecimal.ZERO);
        DEFAULT_BALANCES.put("ARS", BigDecimal.ZERO);
    }

    private DefaultWalletConstants() {
    }
}
