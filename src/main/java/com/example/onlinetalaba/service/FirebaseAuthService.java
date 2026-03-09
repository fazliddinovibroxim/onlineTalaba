package com.example.onlinetalaba.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseAuthService {
    /** 2) Token yaroqli bo‘lsa email, bo‘lmasa null */
    public String emailIfValidOrNull(String idTokenOrBearer) {
        try {
            FirebaseToken t = verify(idTokenOrBearer, true);
            String email = t.getEmail();
            log.info("Token valid, email found: {}", email);
            return (email != null && !email.isBlank()) ? email : null;
        } catch (Exception e) {
            log.warn("Token tekshirishda xato yoki token yaroqsiz: {}", e.getMessage());
            return null;
        }
    }

    /* ================= helpers ================= */

    private FirebaseToken verify(String idTokenOrBearer, boolean checkRevoked) {
        try {
            String token = stripBearer(idTokenOrBearer);
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token, checkRevoked);
            log.info("Token successfully verified for uid: {}", firebaseToken.getUid());
            return firebaseToken;
        } catch (FirebaseAuthException e) {
            log.error("FirebaseAuthException: Token invalid/revoked: {} / {}", e.getAuthErrorCode(), e.getMessage());
            throw new IllegalArgumentException("Token yaroqsiz/muddati o‘tgan/revoke qilingan: " + e.getAuthErrorCode(), e);
        } catch (Exception e) {
            log.error("Exception during token verification: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Token tekshirishda xatolik", e);
        }
    }

    private String stripBearer(String token) {
        if (token == null) {
            log.error("Token null bo'lib kelgan");
            throw new IllegalArgumentException("Token bo'sh bo'lmasin");
        }
        String t = token.replaceFirst("(?i)^Bearer\\s+", "").trim();
        if (t.isEmpty()) {
            log.error("Token bo'sh bo'lib kelgan");
            throw new IllegalArgumentException("Token bo'sh bo'lmasin");
        }
        return t;
    }
}