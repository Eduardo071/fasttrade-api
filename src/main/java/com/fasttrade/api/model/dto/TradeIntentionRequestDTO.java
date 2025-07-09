package com.fasttrade.api.model.dto;

import lombok.Data;

@Data
public class TradeIntentionRequestDTO {
    private String fromCurrency;
    private String toCurrency;
    private Integer amount;
}
