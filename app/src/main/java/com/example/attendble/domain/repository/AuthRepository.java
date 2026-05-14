package com.example.attendble.domain.repository;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.model.User;

/**
 * Contrat d'authentification (login/signup/logout).
 * Implémentations : {@code InMemoryAuthRepository} (actuel), {@code FirebaseAuthRepository} (futur).
 * Le domain ne dépend d'aucune techno — on swap juste l'impl dans {@code ServiceLocator}.
 */
public interface AuthRepository {

    void login(String email, String password, Callback<User> callback);

    void signupProfesseur(String email, String password, Professeur professeur, Callback<Professeur> callback);

    void signupEtudiant(String email, String password, Etudiant etudiant, Callback<Etudiant> callback);

    void logout();

    String getCurrentUserId();

    boolean isLoggedIn();

    /** Persiste le template visage (moyenne L2-normalisée des 5 captures d'enrôlement). */
    void updateFaceEmbedding(String uid, float[] embedding, Callback<Void> callback);
}