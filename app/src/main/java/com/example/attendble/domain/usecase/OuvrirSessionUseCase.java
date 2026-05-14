package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.SessionStatus;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.SessionRepository;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Ouvre une nouvelle session BLE pour une classe : génère code 4 chiffres + beaconUUID
 * + dateOuverture, et persiste avec statut ACTIVE.
 */
public class OuvrirSessionUseCase {

    public static final long CODE_DUREE_MS = 2 * 60 * 1000L;

    private final SessionRepository sessionRepository;
    private final SecureRandom random = new SecureRandom();

    public OuvrirSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void execute(String classeId, Callback<Session> callback) {
        if (classeId == null || classeId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Classe requise"));
            return;
        }
        long now = System.currentTimeMillis();
        Session session = new Session(
                null,
                classeId,
                generateCode4(),
                now + CODE_DUREE_MS,
                UUID.randomUUID().toString(),
                SessionStatus.ACTIVE,
                now,
                null
        );
        sessionRepository.ouvrirSession(session, callback);
    }

    private String generateCode4() {
        return String.format("%04d", random.nextInt(10000));
    }
}
