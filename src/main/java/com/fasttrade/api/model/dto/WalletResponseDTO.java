package com.fasttrade.api.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class WalletResponseDTO {
    private String userId;
    private Map<String, BigDecimal> balances;
    private String updatedAt;
}
