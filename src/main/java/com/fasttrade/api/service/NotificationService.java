package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.exception.FirebaseProcessingException;
import com.fasttrade.api.model.dto.FcmTokenDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {
    @Autowired
    private FirestoreRepository firestoreRepository;

    private void sendNotificationToUser(String userId, String title, String body) {
        try {
            FcmTokenDTO fcmData = firestoreRepository.getDocumentById(
                    CollectionConstants.FCM_TOKENS,
                    userId,
                    FcmTokenDTO.class
            );

            if (fcmData == null || fcmData.getFcmToken() == null || fcmData.getFcmToken().isBlank()) {
                return;
            }

            Message message = Message.builder()
                    .setToken(fcmData.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("🔔 Notificação enviada com sucesso: " + response);

        } catch (InterruptedException | ExecutionException | FirebaseMessagingException e) {
            throw new FirebaseProcessingException("Erro ao enviar notificação FCM.");
        }
    }

    public void notifyUsers(String fromUserId, String toUserId1, String title, String body) {
        sendNotificationToUser(
                fromUserId,
                title,
                body
        );

        sendNotificationToUser(
                toUserId1,
                title,
                body
        );
    }
}
