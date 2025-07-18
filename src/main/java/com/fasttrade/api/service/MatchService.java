package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.enums.NotificationMessageEnum;
import com.fasttrade.api.enums.TradeStatusEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.MatchResponseDTO;
import com.fasttrade.api.model.dto.TradeIntentionDTO;
import com.fasttrade.api.model.dto.TradeNotificationData;
import com.fasttrade.api.model.dto.WalletResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class MatchService {

    @Autowired
    private FirestoreRepository firestoreRepository;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private NotificationService notificationService;

    public Optional<TradeIntentionDTO> findAndMatchTradeWithoutTransaction(
            TradeIntentionDTO newTrade,
            String userId,
            List<TradeNotificationData> notifications
    ) {
        try {
            Optional<TradeIntentionDTO> match = findPossibleTrade(newTrade);

            if (match.isPresent()) {
                TradeIntentionDTO matchedTrade = match.get();

                // Lê carteiras
                WalletResponseDTO walletA = firestoreRepository.getDocumentById(CollectionConstants.WALLETS, newTrade.getUserId(), WalletResponseDTO.class);
                WalletResponseDTO walletB = firestoreRepository.getDocumentById(CollectionConstants.WALLETS, matchedTrade.getUserId(), WalletResponseDTO.class);

                // Atualiza status das intenções
                Map<String, Object> matchUpdate = Map.of("status", TradeStatusEnum.MATCHED.name());
                firestoreRepository.updateDocument(CollectionConstants.TRADE_INTENTIONS, newTrade.getId(), matchUpdate, TradeIntentionDTO.class);
                firestoreRepository.updateDocument(CollectionConstants.TRADE_INTENTIONS, matchedTrade.getId(), matchUpdate, TradeIntentionDTO.class);

                // Atualiza saldos
                balanceService.handleBalanceOperationsWithoutTransaction(walletA, newTrade.getFromCurrency(), newTrade.getAmountFrom(), BalanceOperationEnum.SUBTRACT_BALANCE);
                balanceService.handleBalanceOperationsWithoutTransaction(walletA, newTrade.getToCurrency(), newTrade.getAmountTo(), BalanceOperationEnum.ADD_BALANCE);
                balanceService.handleBalanceOperationsWithoutTransaction(walletB, matchedTrade.getFromCurrency(), matchedTrade.getAmountFrom(), BalanceOperationEnum.SUBTRACT_BALANCE);
                balanceService.handleBalanceOperationsWithoutTransaction(walletB, matchedTrade.getToCurrency(), matchedTrade.getAmountTo(), BalanceOperationEnum.ADD_BALANCE);

                saveMatch(newTrade.getId(), matchedTrade.getId());

                notifications.add(new TradeNotificationData(
                        newTrade.getUserId(),
                        matchedTrade.getUserId(),
                        NotificationMessageEnum.TRADE_COMPLETED_TITLE.getValue(),
                        NotificationMessageEnum.TRADE_COMPLETED_BODY.getValue()
                ));
            }

            return match;
        } catch (Exception e) {
            throw new FirebaseProcessingException("Erro ao processar a intenção de troca.");
        }
    }

    private Optional<TradeIntentionDTO> findPossibleTrade(TradeIntentionDTO newTrade) {
        try {
            List<TradeIntentionDTO> possibleMatches = firestoreRepository.queryMatchingTrade(
                    newTrade.getFromCurrency(),
                    newTrade.getToCurrency(),
                    newTrade.getAmountFrom()
            );

            return possibleMatches.stream()
                    .filter(m -> m.getAmountFrom().equals(newTrade.getAmountTo()) && !m.getUserId().equals(newTrade.getUserId()))
                    .findFirst();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao buscar match.");
        }
    }

    private void saveMatch(String fromIntentionId, String toIntentionId) {
        try {
            String uuid = UUID.randomUUID().toString();
            String createdAt = LocalDateTime.now().toString();

            MatchResponseDTO match = new MatchResponseDTO(uuid, fromIntentionId, toIntentionId, TradeStatusEnum.MATCHED.getDescription(), createdAt);

            firestoreRepository.saveDocument(CollectionConstants.MATCHES, uuid, match, MatchResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao salvar o match.");
        }
    }
}
