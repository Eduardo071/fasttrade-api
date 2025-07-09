package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.exception.UserAlreadyExistsException;
import com.fasttrade.api.exception.UserNotFoundException;
import com.fasttrade.api.model.dto.FcmTokenDTO;
import com.fasttrade.api.model.dto.UserAdditionalDataDTO;
import com.fasttrade.api.model.dto.UserResponseDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    @Autowired
    private FirestoreRepository firestoreRepository;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private WalletService walletService;

    private static final String COLLECTION_NAME = CollectionConstants.USERS;

    public UserResponseDTO authenticateAndGetUser(String token, String fcmToken) {
        String uid = firebaseAuthService.extractTokenUid(token);
        UserResponseDTO user = getUserByUid(uid);

        saveFcmToken(user.getUid(), user.getFcmToken());

        return user;
    }

    public UserResponseDTO registerUser(String token, UserAdditionalDataDTO data) {
        try {
            String uid = firebaseAuthService.extractTokenUid(token);
            String email = firebaseAuthService.extractTokenEmail(token);

            UserResponseDTO existentUser = getUserByUid(uid);

            if (existentUser != null) {
                throw new UserAlreadyExistsException();
            }

            UserResponseDTO newUser = new UserResponseDTO();
            newUser.setUid(uid);
            newUser.setEmail(email);
            newUser.setFcmToken(data.getFcmToken());
            newUser.setFullName(data.getFullName());
            newUser.setMainCurrency(data.getMainCurrency());
            newUser.setCountryId(data.getCountryId());
            newUser.setCreatedAt(LocalDateTime.now().toString());

            walletService.createUserWallet(uid);
            saveFcmToken(newUser.getUid(), newUser.getFcmToken());
            return firestoreRepository.saveDocument(COLLECTION_NAME, uid, newUser, UserResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao cadastrar o usuário no Firestore.");
        }
    }

    public UserResponseDTO updateUser(String token, UserAdditionalDataDTO data) {
        try {
            String uid = firebaseAuthService.extractTokenUid(token);
            UserResponseDTO existentUser = getUserByUid(uid);

            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", data.getFullName());
            updates.put("mainCurrency", data.getMainCurrency());
            updates.put("countryId", data.getCountryId());

            saveFcmToken(existentUser.getUid(), data.getFcmToken());
            return firestoreRepository.updateDocument(COLLECTION_NAME, uid, updates, UserResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao atualizar o usuário no Firestore.");
        }
    }

    public UserResponseDTO getUserByUid(String uid) {
        try {
            UserResponseDTO user = firestoreRepository.getDocumentById(COLLECTION_NAME, uid, UserResponseDTO.class);

            if (user == null) {
                throw new UserNotFoundException();
            }

            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao buscar o usuário por UID no Firestore.");
        }
    }

    private void saveFcmToken(String userId, String fcmToken) {
        try {
            if (fcmToken == null || fcmToken.isBlank()) return;

            FcmTokenDTO fcmData = new FcmTokenDTO(
                    userId,
                    fcmToken,
                    LocalDateTime.now().toString()
            );

            firestoreRepository.saveDocument(
                    CollectionConstants.FCM_TOKENS,
                    userId,
                    fcmData,
                    FcmTokenDTO.class
            );
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao salvar o token FCM.");
        }
    }
}
