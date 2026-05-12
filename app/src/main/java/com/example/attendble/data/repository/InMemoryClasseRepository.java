package com.example.attendble.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Stockage des classes en RAM. Garantit l'unicité du {@code codeInvitation} via un index dédié.
 * À remplacer par {@code FirebaseClasseRepository} (mêmes signatures, écritures dans /classes).
 */
public class InMemoryClasseRepository implements ClasseRepository {

    private static final long FAKE_NETWORK_DELAY_MS = 400;
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_SUFFIX_LENGTH = 3;
    private static final int MAX_GENERATION_RETRIES = 10;

    private final Map<String, Classe> classesById = new HashMap<>();
    private final Map<String, String> codeIndex = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SecureRandom random = new SecureRandom();

    @Override
    public void creerClasse(Classe classe, Callback<Classe> callback) {
        handler.postDelayed(() -> {
            String code = generateUniqueCode(classe.getGroupe(), classe.getNom());
            if (code == null) {
                callback.onError(new IllegalStateException("Impossible de générer un code unique"));
                return;
            }
            classe.setClasseId(UUID.randomUUID().toString());
            classe.setCodeInvitation(code);
            classesById.put(classe.getClasseId(), classe);
            codeIndex.put(code, classe.getClasseId());
            callback.onSuccess(classe);
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void listClassesByProfesseur(String professeurId, Callback<List<Classe>> callback) {
        handler.postDelayed(() -> {
            List<Classe> result = new ArrayList<>();
            for (Classe c : classesById.values()) {
                if (professeurId.equals(c.getProfesseurId())) {
                    result.add(c);
                }
            }
            callback.onSuccess(result);
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void findById(String classeId, Callback<Classe> callback) {
        handler.postDelayed(() -> {
            Classe classe = classeId == null ? null : classesById.get(classeId);
            if (classe == null) {
                callback.onError(new Exception("Classe introuvable"));
                return;
            }
            callback.onSuccess(classe);
        }, FAKE_NETWORK_DELAY_MS);
    }

    @Override
    public void findByCodeInvitation(String codeInvitation, Callback<Classe> callback) {
        handler.postDelayed(() -> {
            String normalized = codeInvitation == null ? "" : codeInvitation.trim().toUpperCase(Locale.ROOT);
            String classeId = codeIndex.get(normalized);
            if (classeId == null) {
                callback.onError(new Exception("Code d'invitation invalide"));
                return;
            }
            callback.onSuccess(classesById.get(classeId));
        }, FAKE_NETWORK_DELAY_MS);
    }

    /**
     * Format : {GROUPE}-{NOM3}-{XXX}, ex. "GL3-ALG-7X2".
     * Retente si collision (très improbable mais on garde la garantie d'unicité).
     */
    private String generateUniqueCode(String groupe, String nom) {
        String groupePart = sanitize(groupe, 4);
        String nomPart = sanitize(nom, 3);
        for (int i = 0; i < MAX_GENERATION_RETRIES; i++) {
            String suffix = randomSuffix();
            String code = groupePart + "-" + nomPart + "-" + suffix;
            if (!codeIndex.containsKey(code)) {
                return code;
            }
        }
        return null;
    }

    private String sanitize(String input, int maxLen) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toUpperCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(c)) sb.append(c);
            if (sb.length() >= maxLen) break;
        }
        return sb.length() == 0 ? "CLS" : sb.toString();
    }

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(CODE_SUFFIX_LENGTH);
        for (int i = 0; i < CODE_SUFFIX_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
