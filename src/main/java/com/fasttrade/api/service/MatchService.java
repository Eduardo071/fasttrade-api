package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.BalanceOperationEnum;
import com.fasttrade.api.enums.NotificationMessageEnum;
import com.fasttrade.api.enums.TradeStatusEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.MatchResponseDTO;
import com.fasttrade.api.model.dto.TradeIntentionDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.google.cloud.firestore.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class MatchService {
    @Autowired
    private FirestoreRepository firestoreRepository;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private NotificationService notificationService;

    public Optional<TradeIntentionDTO> findAndMatchTrade(Transaction transaction, TradeIntentionDTO newTrade, String userId) {
        try {
            Optional<TradeIntentionDTO> match = findPossibleTrade(newTrade);

            if (match.isPresent()) {
                Map<String, Object> matchUpdate = Map.of("status", TradeStatusEnum.MATCHED.name());

                firestoreRepository.updateDocument(transaction, CollectionConstants.TRADE_INTENTIONS, newTrade.getId(), matchUpdate, TradeIntentionDTO.class);
                firestoreRepository.updateDocument(transaction, CollectionConstants.TRADE_INTENTIONS, match.get().getId(), matchUpdate, TradeIntentionDTO.class);

                notificationService.notifyUsers(
                        newTrade.getUserId(),
                        match.get().getUserId(),
                        NotificationMessageEnum.MATCH_FOUND_TITLE.getValue(),
                        NotificationMessageEnum.MATCH_FOUND_BODY.getValue()
                );

                balanceService.handleBalanceOperations(transaction, newTrade.getFromCurrency(), newTrade.getAmountFrom(), userId, BalanceOperationEnum.SUBTRACT_BALANCE);
                balanceService.handleBalanceOperations(transaction, newTrade.getToCurrency(), newTrade.getAmountTo(), userId, BalanceOperationEnum.ADD_BALANCE);

                balanceService.handleBalanceOperations(transaction, match.get().getFromCurrency(), match.get().getAmountFrom(), userId, BalanceOperationEnum.SUBTRACT_BALANCE);
                balanceService.handleBalanceOperations(transaction, match.get().getToCurrency(), match.get().getAmountTo(), userId, BalanceOperationEnum.ADD_BALANCE);

                saveMatch(newTrade.getId(), match.get().getId());
                notificationService.notifyUsers(
                        newTrade.getUserId(),
                        match.get().getUserId(),
                        NotificationMessageEnum.TRADE_COMPLETED_TITLE.getValue(),
                        NotificationMessageEnum.TRADE_COMPLETED_BODY.getValue()
                );
            }

            return match;
        } catch (Error e) {
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
            throw new FirebaseProcessingException("Erro ao processar a intenção de troca.");
        }
    }

    private void saveMatch(String fromIntentionId, String toIntentionId) {
        try {
            String uuid = UUID.randomUUID().toString();
            String createdAt = LocalDateTime.now().toString();

            MatchResponseDTO match = new MatchResponseDTO(uuid, fromIntentionId, toIntentionId, TradeStatusEnum.MATCHED.getDescription(), createdAt);

            firestoreRepository.saveDocument(CollectionConstants.MATCHES, uuid, match, MatchResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao processar a intenção de troca.");
        }
    }
}
