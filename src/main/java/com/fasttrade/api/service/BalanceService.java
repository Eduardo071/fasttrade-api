package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.exception.InvalidBalanceOperationException;
import com.fasttrade.api.model.dto.WalletResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.fasttrade.api.util.CurrencyQuoteUtils;
import com.fasttrade.api.util.MapUtils;
import com.google.cloud.firestore.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BalanceService {
    @Autowired
    private FirestoreRepository firestoreRepository;

    private static final String COLLECTION_NAME = CollectionConstants.WALLETS;

    public void handleBalanceOperations(Transaction transaction, String targetBalanceKey, Integer operationalValue, String userId, BalanceOperationEnum operation) {
        if (operation == null || !EnumSet.allOf(BalanceOperationEnum.class).contains(operation)) {
            throw new InvalidBalanceOperationException();
        }

        updateBalance(transaction, targetBalanceKey, operationalValue, userId, operation);
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


    private void updateBalance(Transaction transaction, String targetBalanceKey, Integer operationalValue, String userId, BalanceOperationEnum operation) {
        try {
            WalletResponseDTO wallet = firestoreRepository.getDocumentById(transaction, COLLECTION_NAME, userId, WalletResponseDTO.class);
            if (wallet == null) {
                throw new FirebaseProcessingException("Carteira do usuário não encontrada.");
            }

            Map<String, BigDecimal> balances = wallet.getBalances();

            BigDecimal currentValue = balances.getOrDefault(targetBalanceKey, BigDecimal.ZERO);
            BigDecimal newValue = getNewValue(BigDecimal.valueOf(operationalValue), operation, currentValue);

            balances.put(targetBalanceKey, newValue);

            Map<String, Object> updates = new HashMap<>();
            updates.put("balances", balances);

            firestoreRepository.updateDocument(transaction, COLLECTION_NAME, userId, updates, WalletResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao atualizar a carteira do usuário no Firestore.");
        }
    }

    private static BigDecimal getNewValue(BigDecimal operationalValue, BalanceOperationEnum operation, BigDecimal currentValue) {
        BigDecimal newValue = BigDecimal.ZERO;

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
