package com.fasttrade.api.service;

import com.fasttrade.api.constant.CollectionConstants;
import com.fasttrade.api.model.dto.FcmTokenDTO;
import com.fasttrade.api.repository.FirestoreRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                System.out.println("⚠️ Token FCM não encontrado para userId: " + userId);
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
            System.out.println("🔔 Notificação enviada com sucesso para " + userId + ": " + response);

        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar notificação para " + userId + ": " + e.getMessage());
        }
    }

    public void notifyUsers(String fromUserId, String toUserId1, String title, String body) {
        sendNotificationToUser(fromUserId, title, body);
        sendNotificationToUser(toUserId1, title, body);
    }
}
