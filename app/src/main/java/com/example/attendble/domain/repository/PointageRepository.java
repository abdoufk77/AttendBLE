package com.example.attendble.domain.repository;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Pointage;

import java.util.List;

/** Contrat de gestion des pointages (validation présence, lecture par session/étudiant). */
public interface PointageRepository {

    /** Insère un pointage PRESENT. {@code codeSaisi} transporte le code 4 chiffres jusqu'au backend
     *  (revérifié serveur-side pour défense en profondeur). Ignoré par l'impl SQLite. */
    void validerPresence(Pointage pointage, String codeSaisi, Callback<Pointage> callback);

    /** Liste les pointages d'une session (live pendant la session, rapport après). */
    void listBySession(String sessionId, Callback<List<Pointage>> callback);

    /** Historique des pointages d'un étudiant (toutes sessions confondues). */
    void listByEtudiant(String etudiantId, Callback<List<Pointage>> callback);

    /** Vrai si l'étudiant a déjà pointé pour cette session. */
    void hasPointage(String sessionId, String etudiantId, Callback<Boolean> callback);
}
