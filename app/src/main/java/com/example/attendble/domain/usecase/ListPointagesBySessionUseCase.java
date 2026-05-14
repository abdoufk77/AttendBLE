package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Pointage;
import com.example.attendble.domain.repository.PointageRepository;

import java.util.List;

/** Liste les pointages d'une session (suivi live côté prof + rapport après fermeture). */
public class ListPointagesBySessionUseCase {

    private final PointageRepository pointageRepository;

    public ListPointagesBySessionUseCase(PointageRepository pointageRepository) {
        this.pointageRepository = pointageRepository;
    }

    public void execute(String sessionId, Callback<List<Pointage>> callback) {
        if (sessionId == null || sessionId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Session requise"));
            return;
        }
        pointageRepository.listBySession(sessionId, callback);
    }
}
