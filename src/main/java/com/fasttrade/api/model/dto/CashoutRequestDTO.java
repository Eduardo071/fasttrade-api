package com.fasttrade.api.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CashoutRequestDTO {
    private String currency;
    private Integer amount;
    private String method;
    private Map<String, String> recipientInfo;
}
