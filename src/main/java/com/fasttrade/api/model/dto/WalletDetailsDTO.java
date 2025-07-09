package com.fasttrade.api.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDetailsDTO {
    private WalletResponseDTO wallet;
    private Map<String, BigDecimal> mainBalances;
    private Map<String, BigDecimal> totalBalanceMainCurrency;
}
