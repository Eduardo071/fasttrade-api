package com.fasttrade.api.model.dto;

import com.fasttrade.api.enums.TradeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeIntentionDTO {
    private String id;
    private String userId;
    private String fromCurrency;
    private String toCurrency;
    private Integer amountFrom;
    private Integer amountTo;
    private TradeStatusEnum status;
    private String createdAt;
}