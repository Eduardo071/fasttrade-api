package com.fasttrade.api.util;

import com.fasttrade.api.enums.CurrencyQuoteEnum;
import com.fasttrade.api.model.dto.ExchangeOptionDTO;

import java.util.ArrayList;
import java.util.List;

public class CurrencyQuoteUtils {

    private static final Integer[] FIXED_VALUES = {10, 20, 50, 100};

    public static List<ExchangeOptionDTO> generateExchangeOptions(CurrencyQuoteEnum from, CurrencyQuoteEnum to) {
        List<ExchangeOptionDTO> options = new ArrayList<>();

        boolean fixInFrom = from.getRateToUSD() < to.getRateToUSD();

        for (Integer fixedAmount : FIXED_VALUES) {
            int fromAmount, toAmount;

            if (fixInFrom) {
                double usd = from.toUSD(fixedAmount);
                double converted = to.fromUSD(usd);

                fromAmount = fixedAmount;
                toAmount = (int) Math.round(converted);
            } else {
                double usd = to.toUSD(fixedAmount);
                double converted = from.fromUSD(usd);

                fromAmount = (int) Math.round(converted);
                toAmount = fixedAmount;
            }

            options.add(new ExchangeOptionDTO(fromAmount, toAmount));
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

