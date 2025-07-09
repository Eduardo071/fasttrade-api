package com.fasttrade.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyQuoteEnum {
    USD(1.00),
    BRL(5.51),
    EUR(0.87),
    ARS(1164.50);

    private final double rateToUSD;

    public double toUSD(double amount) {
        return amount / rateToUSD;
    }

    public double fromUSD(double usdAmount) {
        return usdAmount * rateToUSD;
    }
}
