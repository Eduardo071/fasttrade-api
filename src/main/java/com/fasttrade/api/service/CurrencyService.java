package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.CurrencyDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CurrencyService {
    @Autowired
    private FirestoreRepository firestoreRepository;

    public List<CurrencyDTO> getAllCurrencies() {
        try {
            return firestoreRepository.getAllDocumentsOrdered(CollectionConstants.CURRENCIES, CurrencyDTO.class, "name");
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao buscar moedas no Firestore.");
        }
    }
}
