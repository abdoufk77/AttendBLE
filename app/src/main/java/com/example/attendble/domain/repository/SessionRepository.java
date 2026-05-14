package com.example.attendble.domain.repository;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Session;

import java.util.List;

/** Contrat de gestion des sessions BLE (ouverture, refresh code, fermeture, lecture). */
public interface SessionRepository {

    /** Persiste une nouvelle session ACTIVE (sessionId généré par l'impl). */
    void ouvrirSession(Session session, Callback<Session> callback);

    /** Met à jour le code temporaire + nouvelle expiration. */
    void refreshCode(String sessionId, String nouveauCode, long expireAt, Callback<Session> callback);

    /** Passe la session à FERMEE et fixe dateFermeture. */
    void fermerSession(String sessionId, Callback<Session> callback);

    /** Session ACTIVE d'une classe, ou onError si aucune. */
    void getActiveByClasse(String classeId, Callback<Session> callback);

    /** Retourne la session par id. */
    void findById(String sessionId, Callback<Session> callback);

    /** Historique des sessions d'une classe (toutes, triées date desc). */
    void listByClasse(String classeId, Callback<List<Session>> callback);

    /** Sessions ACTIVES toutes classes confondues (utile côté scanner étudiant). */
    void listAllActive(Callback<List<Session>> callback);
}
