package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.CurrencyQuoteEnum;
import com.fasttrade.api.enums.TradeStatusEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.exception.InsufficientBalanceException;
import com.fasttrade.api.model.dto.*;
import com.fasttrade.api.repository.FirestoreRepository;
import com.fasttrade.api.util.CurrencyQuoteUtils;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class TradeIntentionService {
    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private FirestoreRepository firestoreRepository;

    @Autowired
    private MatchService matchService;

    public TradeIntentionResponseDTO createTradeIntention(TradeIntentionRequestDTO trade, String token) {
        try {
            return FirestoreClient.getFirestore().runTransaction(transaction -> {
                String userId = firebaseAuthService.extractTokenUid(token);

                WalletResponseDTO wallet = firestoreRepository.getDocumentById(transaction, CollectionConstants.WALLETS, userId, WalletResponseDTO.class);
                if (wallet == null) throw new FirebaseProcessingException("Carteira não encontrada.");

                BigDecimal balance = wallet.getBalances().getOrDefault(trade.getFromCurrency(), BigDecimal.ZERO);
                if (balance.compareTo(BigDecimal.valueOf(trade.getAmount())) < 0) {
                    throw new InsufficientBalanceException();
                }

                String tradeId = UUID.randomUUID().toString();
                Integer amountTo = CurrencyQuoteUtils.convert(trade.getFromCurrency(), trade.getToCurrency(), trade.getAmount());
                TradeIntentionDTO newTrade = new TradeIntentionDTO(
                        tradeId,
                        userId,
                        trade.getFromCurrency(),
                        trade.getToCurrency(),
                        trade.getAmount(),
                        amountTo,
                        TradeStatusEnum.PENDING,
                        LocalDateTime.now().toString()
                );

                firestoreRepository.saveDocument(transaction, CollectionConstants.TRADE_INTENTIONS, tradeId, newTrade, TradeIntentionDTO.class);

                Optional<TradeIntentionDTO> match = matchService.findAndMatchTrade(transaction, newTrade, userId);

                return new TradeIntentionResponseDTO(
                        newTrade.getId(),
                        newTrade.getFromCurrency(),
                        newTrade.getToCurrency(),
                        newTrade.getAmountFrom(),
                        newTrade.getAmountTo(),
                        match.isPresent() ? TradeStatusEnum.MATCHED : TradeStatusEnum.PENDING
                );
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao processar a intenção de troca.");
        }
    }

    public List<ExchangeOptionDTO> getTradeValues(TradeIntentionRequestDTO trade, String token) {
        CurrencyQuoteEnum from = CurrencyQuoteEnum.valueOf(trade.getFromCurrency());
        CurrencyQuoteEnum to = CurrencyQuoteEnum.valueOf(trade.getToCurrency());

        return CurrencyQuoteUtils.generateExchangeOptions(from, to);
    }
}
