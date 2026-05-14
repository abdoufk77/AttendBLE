package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.SessionRepository;

import java.util.List;

/** Historique des sessions d'une classe (rapport). */
public class ListSessionsByClasseUseCase {

    private final SessionRepository sessionRepository;

    public ListSessionsByClasseUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void execute(String classeId, Callback<List<Session>> callback) {
        if (classeId == null || classeId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Classe requise"));
            return;
        }
        sessionRepository.listByClasse(classeId, callback);
    }
}
