package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

import java.util.List;

/** Liste les classes appartenant au professeur connecté. */
public class ListClassesByProfUseCase {

    private final ClasseRepository classeRepository;

    public ListClassesByProfUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String professeurId, Callback<List<Classe>> callback) {
        if (professeurId == null || professeurId.isEmpty()) {
            callback.onError(new IllegalStateException("Professeur non authentifié"));
            return;
        }
        classeRepository.listClassesByProfesseur(professeurId, callback);
    }
}
