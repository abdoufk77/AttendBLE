package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

/** Charge une classe par id (écran détails). */
public class GetClasseUseCase {

    private final ClasseRepository classeRepository;

    public GetClasseUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String classeId, Callback<Classe> callback) {
        if (classeId == null || classeId.isEmpty()) {
            callback.onError(new IllegalArgumentException("classeId manquant"));
            return;
        }
        classeRepository.findById(classeId, callback);
    }
}
