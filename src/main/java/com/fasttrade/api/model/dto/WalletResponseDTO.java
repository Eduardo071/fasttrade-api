package com.fasttrade.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponseDTO {
    private String userId;
    private Map<String, BigDecimal> balances;
    private String updatedAt;
}
