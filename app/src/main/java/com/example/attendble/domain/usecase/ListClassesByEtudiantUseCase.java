package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

import java.util.List;

/** Liste les classes dans lesquelles l'étudiant connecté est inscrit. */
public class ListClassesByEtudiantUseCase {

    private final ClasseRepository classeRepository;

    public ListClassesByEtudiantUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String etudiantId, Callback<List<Classe>> callback) {
        if (etudiantId == null || etudiantId.isEmpty()) {
            callback.onError(new IllegalStateException("Étudiant non authentifié"));
            return;
        }
        classeRepository.listClassesByEtudiant(etudiantId, callback);
    }
}
