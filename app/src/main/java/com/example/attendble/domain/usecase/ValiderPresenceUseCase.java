package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.PointageStatut;
import com.example.attendble.domain.enums.SessionStatus;
import com.example.attendble.domain.model.Pointage;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.PointageRepository;
import com.example.attendble.domain.repository.SessionRepository;

/**
 * Valide la présence d'un étudiant : vérifie session ACTIVE + code correct + non expiré
 * + détection BLE + face match, puis enregistre un pointage PRESENT.
 */
public class ValiderPresenceUseCase {

    private final SessionRepository sessionRepository;
    private final PointageRepository pointageRepository;

    public ValiderPresenceUseCase(SessionRepository sessionRepository,
                                  PointageRepository pointageRepository) {
        this.sessionRepository = sessionRepository;
        this.pointageRepository = pointageRepository;
    }

    public void execute(String sessionId, String etudiantId, String codeSaisi,
                        boolean bleDetecte, boolean faceVerified, Callback<Pointage> callback) {
        if (!bleDetecte) {
            callback.onError(new IllegalStateException("Aucune session détectée"));
            return;
        }
        if (!faceVerified) {
            callback.onError(new IllegalStateException("Visage non reconnu"));
            return;
        }
        if (codeSaisi == null || codeSaisi.trim().length() != 4) {
            callback.onError(new IllegalArgumentException("Code invalide"));
            return;
        }
        sessionRepository.findById(sessionId, new Callback<Session>() {
            @Override
            public void onSuccess(Session session) {
                if (session.getStatut() != SessionStatus.ACTIVE) {
                    callback.onError(new IllegalStateException("Session fermée"));
                    return;
                }
                if (System.currentTimeMillis() > session.getCodeExpireAt()) {
                    callback.onError(new IllegalStateException("Code expiré"));
                    return;
                }
                if (!session.getCodeTemp().equals(codeSaisi.trim())) {
                    callback.onError(new IllegalStateException("Code invalide"));
                    return;
                }
                Pointage pointage = new Pointage(
                        null, sessionId, etudiantId, System.currentTimeMillis(),
                        PointageStatut.PRESENT, true, true);
                pointageRepository.validerPresence(pointage, codeSaisi.trim(), callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
