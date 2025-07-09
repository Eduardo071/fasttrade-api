package com.fasttrade.api.util;

import com.fasttrade.api.enums.CurrencyQuoteEnum;
import com.fasttrade.api.model.dto.ExchangeOptionDTO;

import java.util.ArrayList;
import java.util.List;

public class CurrencyQuoteUtils {

    private static final Integer[] FIXED_VALUES = {10, 20, 50, 100};

    public static List<ExchangeOptionDTO> generateExchangeOptions(CurrencyQuoteEnum from, CurrencyQuoteEnum to) {
        List<ExchangeOptionDTO> options = new ArrayList<>();

        for (Integer amount : FIXED_VALUES) {
            double amountInUSD = CurrencyQuoteEnum.USD.toUSD(amount);
            double amountFrom = from.fromUSD(amountInUSD);
            double amountTo = to.fromUSD(amountInUSD);

            Integer roundedFrom = (int) Math.round(amountFrom);
            Integer roundedTo = (int) Math.round(amountTo);

            options.add(new ExchangeOptionDTO(roundedFrom, roundedTo));
        }

        return options;
    }

    public static Integer convert(String fromCurrencyCode, String toCurrencyCode, Integer amountFrom) {
        CurrencyQuoteEnum from = CurrencyQuoteEnum.valueOf(fromCurrencyCode);
        CurrencyQuoteEnum to = CurrencyQuoteEnum.valueOf(toCurrencyCode);

        double amountInUSD = from.toUSD(amountFrom);
        double amountTo = to.fromUSD(amountInUSD);

        return (int) Math.round(amountTo);
    }
}

