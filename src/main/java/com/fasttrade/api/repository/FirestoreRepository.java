package com.fasttrade.api.repository;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.enums.TradeStatusEnum;
import com.fasttrade.api.model.dto.TradeIntentionDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreRepository {

    // === MÉTODO SEM TRANSAÇÃO ===
    public <T> T getDocumentById(String collection, String id, Class<T> clazz) throws ExecutionException, InterruptedException {
        DocumentReference ref = FirestoreClient.getFirestore().collection(collection).document(id);
        DocumentSnapshot snapshot = ref.get().get();
        return snapshot.exists() ? snapshot.toObject(clazz) : null;
    }

    public <T> List<T> getAllDocumentsOrdered(String collectionName, Class<T> clazz, String field) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collection = db.collection(collectionName);
        ApiFuture<QuerySnapshot> future = collection.orderBy(field).get();

        List<T> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            T obj = doc.toObject(clazz);
            result.add(obj);
        }

        return result;
    }

    public <T> T saveDocument(String collection, String id, T data, Class<T> clazz) throws ExecutionException, InterruptedException {
        DocumentReference ref = FirestoreClient.getFirestore().collection(collection).document(id);
        ref.set(data);
        DocumentSnapshot snapshot = ref.get().get();
        return snapshot.exists() ? snapshot.toObject(clazz) : null;
    }

    public <T> T updateDocument(String collection, String id, Map<String, Object> updates, Class<T> clazz) throws ExecutionException, InterruptedException {
        DocumentReference ref = FirestoreClient.getFirestore().collection(collection).document(id);
        ref.update(updates);
        DocumentSnapshot snapshot = ref.get().get();
        return snapshot.exists() ? snapshot.toObject(clazz) : null;
    }

    // === COM TRANSAÇÃO ===
    public <T> T getDocumentById(Transaction transaction, String collection, String id, Class<T> clazz) throws ExecutionException, InterruptedException {
        DocumentReference ref = FirestoreClient.getFirestore().collection(collection).document(id);
        DocumentSnapshot snapshot = transaction.get(ref).get();
        return snapshot.exists() ? snapshot.toObject(clazz) : null;
    }

    public <T> void saveDocument(Transaction transaction, String collection, String id, T data, Class<T> clazz) throws ExecutionException, InterruptedException {
        DocumentReference ref = FirestoreClient.getFirestore().collection(collection).document(id);
        transaction.set(ref, data);
    }

    public <T> void updateDocument(Transaction transaction, String collection, String id, Map<String, Object> updates, Class<T> clazz) {
        DocumentReference ref = FirestoreClient.getFirestore().collection(collection).document(id);
        transaction.update(ref, updates);
    }

    public List<TradeIntentionDTO> queryMatchingTrade(String fromCurrency, String toCurrency, Integer amount) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(CollectionConstants.TRADE_INTENTIONS)
                .whereEqualTo("fromCurrency", toCurrency)
                .whereEqualTo("toCurrency", fromCurrency)
                .whereEqualTo("amountTo", amount)
                .whereEqualTo("status", TradeStatusEnum.PENDING.name())
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<TradeIntentionDTO> matches = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            matches.add(doc.toObject(TradeIntentionDTO.class));
        }

        return matches;
    }
}
