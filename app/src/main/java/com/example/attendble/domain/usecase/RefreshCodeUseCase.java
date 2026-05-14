package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.SessionRepository;

import java.security.SecureRandom;

/** Régénère le code temporaire d'une session ACTIVE (toutes les 2 min côté UI). */
public class RefreshCodeUseCase {

    private final SessionRepository sessionRepository;
    private final SecureRandom random = new SecureRandom();

    public RefreshCodeUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void execute(String sessionId, Callback<Session> callback) {
        if (sessionId == null || sessionId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Session requise"));
            return;
        }
        String nouveauCode = String.format("%04d", random.nextInt(10000));
        long expireAt = System.currentTimeMillis() + OuvrirSessionUseCase.CODE_DUREE_MS;
        sessionRepository.refreshCode(sessionId, nouveauCode, expireAt, callback);
    }
}
