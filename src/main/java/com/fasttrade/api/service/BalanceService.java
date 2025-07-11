package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.WalletResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.fasttrade.api.util.CurrencyQuoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BalanceService {
    @Autowired
    private FirestoreRepository firestoreRepository;

    private static final String COLLECTION_NAME = CollectionConstants.WALLETS;

    public void handleBalanceOperations(
            com.google.cloud.firestore.Transaction transaction,
            WalletResponseDTO wallet,
            String targetBalanceKey,
            Integer operationalValue,
            BalanceOperationEnum operation
    ) {
        Map<String, BigDecimal> balances = wallet.getBalances();

        BigDecimal currentValue = balances.getOrDefault(targetBalanceKey, BigDecimal.ZERO);
        BigDecimal newValue = getNewValue(BigDecimal.valueOf(operationalValue), operation, currentValue);

        balances.put(targetBalanceKey, newValue);

        Map<String, Object> updates = new HashMap<>();
        updates.put("balances", balances);

        firestoreRepository.updateDocument(transaction, COLLECTION_NAME, wallet.getUserId(), updates, WalletResponseDTO.class);
    }

    // NOVO MÉTODO SEM TRANSAÇÃO
    public void handleBalanceOperationsWithoutTransaction(
            WalletResponseDTO wallet,
            String targetBalanceKey,
            Integer operationalValue,
            BalanceOperationEnum operation
    ) {
        try {
            Map<String, BigDecimal> balances = wallet.getBalances();
            BigDecimal currentValue = balances.getOrDefault(targetBalanceKey, BigDecimal.ZERO);
            BigDecimal newValue = getNewValue(BigDecimal.valueOf(operationalValue), operation, currentValue);

            balances.put(targetBalanceKey, newValue);

            Map<String, Object> updates = new HashMap<>();
            updates.put("balances", balances);

            firestoreRepository.updateDocument(COLLECTION_NAME, wallet.getUserId(), updates, WalletResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao atualizar saldo.");
        }
    }

    public Map<String, BigDecimal> getMainBalances(Map<String, BigDecimal> balances, String mainCurrency) {
        Map<String, BigDecimal> result = new HashMap<>();

        if (balances.containsKey(mainCurrency)) {
            result.put(mainCurrency, balances.get(mainCurrency));
        }

        BigDecimal maxValue = BigDecimal.ZERO;
        String maxKey = null;

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            if (!entry.getKey().equals(mainCurrency) && entry.getValue().compareTo(maxValue) > 0) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }

        if (maxKey != null) {
            result.put(maxKey, maxValue);
        }

        return result;
    }

    public Map<String, BigDecimal> calculateTotalBalanceMainCurrency(Map<String, BigDecimal> balances, String mainCurrency) {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            String currency = entry.getKey();
            BigDecimal amount = entry.getValue();

            BigDecimal converted = BigDecimal.valueOf(
                    CurrencyQuoteUtils.convert(currency, mainCurrency, amount.intValue())
            );

            total = total.add(converted);
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put(mainCurrency, total);

        return result;
    }

    private static BigDecimal getNewValue(BigDecimal operationalValue, BalanceOperationEnum operation, BigDecimal currentValue) {
        BigDecimal newValue;

        if (operation.equals(BalanceOperationEnum.ADD_BALANCE)) {
            newValue = currentValue.add(operationalValue);
        } else if (operation.equals(BalanceOperationEnum.SUBTRACT_BALANCE)) {
            if (currentValue.compareTo(operationalValue) < 0) {
                throw new FirebaseProcessingException("Saldo insuficiente para realizar o saque.");
            }
            newValue = currentValue.subtract(operationalValue);
        } else {
            throw new IllegalArgumentException("Operação inválida para saldo.");
        }
        return newValue;
    }
}
