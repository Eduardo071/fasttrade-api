package com.fasttrade.api.service;

import com.fasttrade.api.exception.InvalidTokenException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {
    public String extractTokenUid(String token) {
        return verifyToken(token).getUid();
    }

    public String extractTokenEmail(String token) {
        return verifyToken(token).getEmail();
    }

    private FirebaseToken verifyToken(String token) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(token);
        } catch (FirebaseAuthException e) {
            throw new InvalidTokenException();
        }
    }
}
