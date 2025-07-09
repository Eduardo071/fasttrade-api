package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.enums.CashoutStatusEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.CashoutRequestDTO;
import com.fasttrade.api.model.dto.CashoutResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class CashoutService {
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private FirestoreRepository firestoreRepository;

    private static final String COLLECTION_NAME = CollectionConstants.CASHOUTS;

    public CashoutResponseDTO postCashout(String token, CashoutRequestDTO cashoutRequestDTO) {
        try {
            return FirestoreClient.getFirestore().runTransaction(transaction -> {
                String userId = firebaseAuthService.extractTokenUid(token);
                String cashoutId = UUID.randomUUID().toString();
                CashoutResponseDTO cashoutRegister = new CashoutResponseDTO(
                        cashoutId,
                        userId,
                        cashoutRequestDTO.getCurrency(),
                        cashoutRequestDTO.getAmount(),
                        cashoutRequestDTO.getMethod(),
                        cashoutRequestDTO.getRecipientInfo(),
                        CashoutStatusEnum.PENDING.getDescription(),
                        LocalDateTime.now().toString()
                );

                CashoutResponseDTO cashoutCreated = firestoreRepository.saveDocument(transaction, COLLECTION_NAME, cashoutId, cashoutRegister, CashoutResponseDTO.class);

                balanceService.handleBalanceOperations(transaction, cashoutRequestDTO.getCurrency(), cashoutRequestDTO.getAmount(), userId, BalanceOperationEnum.SUBTRACT_BALANCE);

                return cashoutCreated;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao processar o saque.");
        }
    }
}