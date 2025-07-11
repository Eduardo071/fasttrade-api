package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.enums.CashoutStatusEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.CashoutRequestDTO;
import com.fasttrade.api.model.dto.CashoutResponseDTO;
import com.fasttrade.api.model.dto.WalletResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class CashoutService {
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private FirestoreRepository firestoreRepository;

    private static final String COLLECTION_NAME = CollectionConstants.CASHOUTS;
    private static final String WALLETS_COLLECTION_NAME = CollectionConstants.WALLETS;

    public CashoutResponseDTO postCashout(String email, CashoutRequestDTO cashoutRequestDTO) {
        try {
            return FirestoreClient.getFirestore().runTransaction(transaction -> {
                WalletResponseDTO wallet = firestoreRepository.getDocumentById(transaction, WALLETS_COLLECTION_NAME, email, WalletResponseDTO.class);
                if (wallet == null) {
                    throw new FirebaseProcessingException("Carteira do usuário não encontrada.");
                }

                Map<String, BigDecimal> balances = wallet.getBalances();
                String targetCurrency = cashoutRequestDTO.getCurrency();
                BigDecimal currentValue = balances.getOrDefault(targetCurrency, BigDecimal.ZERO);
                BigDecimal amount = BigDecimal.valueOf(cashoutRequestDTO.getAmount());

                if (currentValue.compareTo(amount) < 0) {
                    throw new FirebaseProcessingException("Saldo insuficiente para realizar o saque.");
                }

                BigDecimal newValue = currentValue.subtract(amount);
                balances.put(targetCurrency, newValue);

                Map<String, Object> updates = new HashMap<>();
                updates.put("balances", balances);

                String cashoutId = UUID.randomUUID().toString();
                CashoutResponseDTO cashoutRegister = new CashoutResponseDTO(
                        cashoutId,
                        email,
                        cashoutRequestDTO.getCurrency(),
                        cashoutRequestDTO.getAmount(),
                        cashoutRequestDTO.getMethod(),
                        cashoutRequestDTO.getRecipientInfo(),
                        CashoutStatusEnum.PENDING.getDescription(),
                        LocalDateTime.now().toString()
                );

                firestoreRepository.updateDocument(transaction, WALLETS_COLLECTION_NAME, email, updates, WalletResponseDTO.class);
                firestoreRepository.saveDocument(transaction, COLLECTION_NAME, cashoutId, cashoutRegister, CashoutResponseDTO.class);

                return cashoutRegister;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao processar o saque.");
        }
    }
}