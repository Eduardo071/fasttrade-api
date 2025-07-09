package com.fasttrade.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExchangeOptionDTO {
    private Integer offerAmount;
    private Integer receiveAmount;
}
