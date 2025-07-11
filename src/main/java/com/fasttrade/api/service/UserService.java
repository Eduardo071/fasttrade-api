package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.exception.UserAlreadyExistsException;
import com.fasttrade.api.exception.UserNotFoundException;
import com.fasttrade.api.model.dto.FcmTokenDTO;
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
    private WalletService walletService;

    private static final String COLLECTION_NAME = CollectionConstants.USERS;

    public UserResponseDTO authenticateAndGetUser(UserResponseDTO data) {
        try {
            UserResponseDTO user = getUserByEmail(data.getEmail());

            if (!user.getPassword().equals(data.getPassword())) {
                throw new IllegalArgumentException("Senha inválida");
            }

            saveFcmToken(user.getEmail(), data.getFcmToken());

            return user;
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    public UserResponseDTO registerUser(UserResponseDTO data) {
        try {

            UserResponseDTO existentUser = findUserIfExists(data.getEmail());

            if (existentUser != null) {
                throw new UserAlreadyExistsException();
            }

            UserResponseDTO newUser = new UserResponseDTO();
            newUser.setEmail(data.getEmail());
            newUser.setPassword(data.getPassword());
            newUser.setFcmToken(data.getFcmToken());
            newUser.setFullName(data.getFullName());
            newUser.setMainCurrency(data.getMainCurrency());
            newUser.setCountryId(data.getCountryId());
            newUser.setCreatedAt(LocalDateTime.now().toString());

            walletService.createUserWallet(data.getEmail());
            saveFcmToken(newUser.getEmail(), newUser.getFcmToken());
            return firestoreRepository.saveDocument(COLLECTION_NAME, data.getEmail(), newUser, UserResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao cadastrar o usuário no Firestore.");
        }
    }

    public UserResponseDTO updateUser(UserResponseDTO data) {
        try {
            UserResponseDTO existentUser = getUserByEmail(data.getEmail());

            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", data.getFullName());
            updates.put("mainCurrency", data.getMainCurrency());
            updates.put("countryId", data.getCountryId());

            saveFcmToken(existentUser.getEmail(), data.getFcmToken());
            return firestoreRepository.updateDocument(COLLECTION_NAME, existentUser.getEmail(), updates, UserResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao atualizar o usuário no Firestore.");
        }
    }

    public UserResponseDTO getUserByEmail(String email) {
        try {
            UserResponseDTO user = firestoreRepository.getDocumentById(COLLECTION_NAME, email, UserResponseDTO.class);

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

    private UserResponseDTO findUserIfExists(String email) {
        try {
            return firestoreRepository.getDocumentById(COLLECTION_NAME, email, UserResponseDTO.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new FirebaseProcessingException("Erro ao buscar o usuário no Firestore.");
        }
    }
}
