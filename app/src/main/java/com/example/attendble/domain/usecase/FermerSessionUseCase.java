package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.SessionRepository;

/** Ferme une session : passe à FERMEE + fixe dateFermeture. */
public class FermerSessionUseCase {

    private final SessionRepository sessionRepository;

    public FermerSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void execute(String sessionId, Callback<Session> callback) {
        if (sessionId == null || sessionId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Session requise"));
            return;
        }
        sessionRepository.fermerSession(sessionId, callback);
    }
}
