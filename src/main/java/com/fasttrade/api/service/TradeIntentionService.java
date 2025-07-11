package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.CurrencyQuoteEnum;
import com.fasttrade.api.enums.TradeStatusEnum;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.exception.InsufficientBalanceException;
import com.fasttrade.api.model.dto.*;
import com.fasttrade.api.repository.FirestoreRepository;
import com.fasttrade.api.util.CurrencyQuoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TradeIntentionService {

    @Autowired
    private FirestoreRepository firestoreRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private NotificationService notificationService;

    public TradeIntentionResponseDTO createTradeIntention(TradeIntentionRequestDTO trade, String email) {
        try {
            List<TradeNotificationData> notificationsToSend = new ArrayList<>();

            // Obtém carteira
            WalletResponseDTO wallet = firestoreRepository.getDocumentById(CollectionConstants.WALLETS, email, WalletResponseDTO.class);
            if (wallet == null) throw new FirebaseProcessingException("Carteira não encontrada.");

            // Valida saldo
            BigDecimal balance = wallet.getBalances().getOrDefault(trade.getFromCurrency(), BigDecimal.ZERO);
            Integer requiredInFromCurrency = CurrencyQuoteUtils.convert(trade.getToCurrency(), trade.getFromCurrency(), trade.getAmount());
            if (balance.compareTo(BigDecimal.valueOf(requiredInFromCurrency)) < 0) {
                throw new InsufficientBalanceException();
            }

            // Cria intenção de troca
            String tradeId = UUID.randomUUID().toString();
            Integer amountTo = CurrencyQuoteUtils.convert(trade.getFromCurrency(), trade.getToCurrency(), requiredInFromCurrency);

            TradeIntentionDTO newTrade = new TradeIntentionDTO(
                    tradeId,
                    email,
                    trade.getFromCurrency(),
                    trade.getToCurrency(),
                    requiredInFromCurrency,
                    amountTo,
                    TradeStatusEnum.PENDING,
                    LocalDateTime.now().toString()
            );

            // Tenta match
            Optional<TradeIntentionDTO> match = matchService.findAndMatchTradeWithoutTransaction(newTrade, email, notificationsToSend);

            // Salva intenção
            firestoreRepository.saveDocument(CollectionConstants.TRADE_INTENTIONS, tradeId, newTrade, TradeIntentionDTO.class);

            // Envia notificações
            for (TradeNotificationData n : notificationsToSend) {
                notificationService.notifyUsers(n.getUserId1(), n.getUserId2(), n.getTitle(), n.getBody());
            }

            return new TradeIntentionResponseDTO(
                    newTrade.getId(),
                    newTrade.getFromCurrency(),
                    newTrade.getToCurrency(),
                    newTrade.getAmountFrom(),
                    newTrade.getAmountTo(),
                    match.isPresent() ? TradeStatusEnum.MATCHED : TradeStatusEnum.PENDING
            );

        } catch (Exception e) {
            throw new FirebaseProcessingException("Erro ao processar a intenção de troca.");
        }
    }

    public List<ExchangeOptionDTO> getTradeValues(TradeIntentionRequestDTO trade) {
        CurrencyQuoteEnum from = CurrencyQuoteEnum.valueOf(trade.getFromCurrency());
        CurrencyQuoteEnum to = CurrencyQuoteEnum.valueOf(trade.getToCurrency());

        return CurrencyQuoteUtils.generateExchangeOptions(from, to);
    }
}
