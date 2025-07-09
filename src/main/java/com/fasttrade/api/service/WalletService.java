package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.constant.DefaultWalletConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.UserResponseDTO;
import com.fasttrade.api.model.dto.WalletDetailsDTO;
import com.fasttrade.api.model.dto.WalletResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.google.cloud.firestore.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

@Service
public class WalletService {
    @Autowired
    private BalanceService balanceService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private FirestoreRepository firestoreRepository;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    private static final String COLLECTION_NAME = CollectionConstants.WALLETS;

    public WalletDetailsDTO getWalletDetails(String token) {
        try {
            String uid = firebaseAuthService.extractTokenUid(token);
            WalletResponseDTO walletResponseDTO = firestoreRepository.getDocumentById(COLLECTION_NAME, uid, WalletResponseDTO.class);
            UserResponseDTO user = userService.getUserByUid(uid);

            return new WalletDetailsDTO(
                    walletResponseDTO,
                    balanceService.getMainBalances(walletResponseDTO.getBalances(), user.getMainCurrency()),
                    balanceService.calculateTotalBalanceMainCurrency(walletResponseDTO.getBalances(), user.getMainCurrency())
            );
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao acessar o Firestore.");
        }
    }

    public void createUserWallet(String uid) {
        try {
            WalletResponseDTO newWallet = WalletResponseDTO.builder()
                    .userId(uid)
                    .balances(DefaultWalletConstants.DEFAULT_BALANCES)
                    .updatedAt(DefaultWalletConstants.DATE_TIME_NOW)
                    .build();

            firestoreRepository.saveDocument(COLLECTION_NAME, uid, newWallet, WalletResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao acessar o Firestore.");
        }
    }
}
