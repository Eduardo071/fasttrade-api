package com.fasttrade.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CashoutResponseDTO {
    private String id;
    private String userId;
    private String currency;
    private Integer amount;
    private String method;
    private Map<String, String> recipientInfo;
    private String status;
    private String requestedAt;
}
