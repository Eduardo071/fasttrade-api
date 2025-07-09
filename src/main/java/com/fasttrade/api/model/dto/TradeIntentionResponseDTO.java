package com.fasttrade.api.model.dto;

import com.fasttrade.api.enums.TradeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeIntentionResponseDTO {
    private String id;
    private String fromCurrency;
    private String toCurrency;
    private Integer amountFrom;
    private Integer amountTo;
    private TradeStatusEnum status;
}