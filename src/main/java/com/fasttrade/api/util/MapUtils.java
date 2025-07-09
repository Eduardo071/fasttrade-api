package com.fasttrade.api.util;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class MapUtils {
    public Map.Entry<String, BigDecimal> getMajorValueInMap(Map<String, BigDecimal> map) {
        Map.Entry<String, BigDecimal> majorNumber = map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (majorNumber == null || majorNumber.getValue().equals(BigDecimal.ZERO)) {
            return getFirstAlphabeticalKey(map);
        }

        return majorNumber;
    }

    public Map.Entry<String, BigDecimal> findMapByKey(Map<String, BigDecimal> map, String key) {
        return map.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    private Map.Entry<String, BigDecimal> getFirstAlphabeticalKey(Map<String, BigDecimal> map) {
        return map.entrySet()
                .stream()
                .min(Map.Entry.comparingByKey())
                .orElse(null);
    }
}
