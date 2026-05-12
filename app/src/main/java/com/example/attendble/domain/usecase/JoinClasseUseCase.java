package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

/** Use case : étudiant rejoint une classe via son code d'invitation. */
public class JoinClasseUseCase {

    private final ClasseRepository classeRepository;

    public JoinClasseUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String codeInvitation, String etudiantId, Callback<Classe> callback) {
        if (codeInvitation == null || codeInvitation.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Code d'invitation requis"));
            return;
        }
        if (etudiantId == null || etudiantId.isEmpty()) {
            callback.onError(new IllegalStateException("Étudiant non authentifié"));
            return;
        }
        classeRepository.joinClasseByCode(codeInvitation.trim(), etudiantId, callback);
    }
}
