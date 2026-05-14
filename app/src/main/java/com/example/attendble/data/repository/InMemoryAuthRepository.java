package com.example.attendble.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.model.User;
import com.example.attendble.domain.repository.AuthRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation temporaire stockant les comptes en RAM (HashMap).
 * Simule un délai réseau via Handler pour reproduire le comportement async de Firebase.
 * À remplacer par {@code FirebaseAuthRepository} en fin de projet (mêmes signatures).
 */
public class InMemoryAuthRepository implements AuthRepository {

    private static class Record {
        final String password;
        final User user;

        Record(String password, User user) {
            this.password = password;
            this.user = user;
        }
    }

    private static final long FAKE_NETWORK_DELAY_MS = 600;

    private final Map<String, Record> usersByEmail = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String currentUserId;

    public InMemoryAuthRepository() {
        seedTestAccounts();
    }

    /** Comptes de test pré-chargés pour éviter de re-signup à chaque lancement. */
    private void seedTestAccounts() {
        Etudiant abdou = new Etudiant(
                UUID.randomUUID().toString(),
                "ab.foukahy@gmail.com",
                "abdou",
                null,
                "123456"
        );
        usersByEmail.put(abdou.getEmail(), new Record("abdou7", abdou));

        Professeur hassan = new Professeur(
                UUID.randomUUID().toString(),
                "prof@gmail.com",
                "hassan",
                null,
                "info"
        );
        usersByEmail.put(hassan.getEmail(), new Record("proof7", hassan));
    }

    @Override
    public void login(String email, String password, Callback<User> callback) {
        handler.postDelayed(() -> {
            Record record = usersByEmail.get(email);
            if (record == null) {
                callback.onError(new Exception("Email introuvable"));
                return;
            }
            if (!record.password.equals(password)) {
                callback.onError(new Exception("Mot de passe incorrect"));
                return;
            }
            currentUserId = record.user.getUid();
            callback.onSuccess(record.user);
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void signupProfesseur(String email, String password, Professeur professeur, Callback<Professeur> callback) {
        handler.postDelayed(() -> {
            if (usersByEmail.containsKey(email)) {
                callback.onError(new Exception("Email déjà utilisé"));
                return;
            }
            professeur.setUid(UUID.randomUUID().toString());
            usersByEmail.put(email, new Record(password, professeur));
            currentUserId = professeur.getUid();
            callback.onSuccess(professeur);
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void signupEtudiant(String email, String password, Etudiant etudiant, Callback<Etudiant> callback) {
        handler.postDelayed(() -> {
            if (usersByEmail.containsKey(email)) {
                callback.onError(new Exception("Email déjà utilisé"));
                return;
            }
            etudiant.setUid(UUID.randomUUID().toString());
            usersByEmail.put(email, new Record(password, etudiant));
            currentUserId = etudiant.getUid();
            callback.onSuccess(etudiant);
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void logout() {
        currentUserId = null;
    }

    @Override
    public String getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    @Override
    public void getCurrentUser(Callback<User> callback) {
        handler.postDelayed(() -> {
            if (currentUserId == null) {
                callback.onError(new Exception("Pas d'utilisateur connecté"));
                return;
            }
            for (Record r : usersByEmail.values()) {
                if (r.user.getUid().equals(currentUserId)) {
                    callback.onSuccess(r.user);
                    return;
                }
            }
            callback.onError(new Exception("Utilisateur introuvable : " + currentUserId));
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void updateFaceEmbedding(String uid, float[] embedding, Callback<Void> callback) {
        handler.postDelayed(() -> {
            for (Record r : usersByEmail.values()) {
                if (r.user.getUid().equals(uid) && r.user instanceof Etudiant) {
                    ((Etudiant) r.user).setFaceEmbedding(embedding);
                    callback.onSuccess(null);
                    return;
                }
            }
            callback.onError(new Exception("Étudiant introuvable : " + uid));
        }, FAKE_NETWORK_DELAY_MS);
    }
}
