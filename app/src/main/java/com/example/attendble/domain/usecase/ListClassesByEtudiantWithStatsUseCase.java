package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.repository.ClasseRepository;

import java.util.List;

/** Classes de l'étudiant + son propre taux de présence dans chacune. */
public class ListClassesByEtudiantWithStatsUseCase {

    private final ClasseRepository classeRepository;

    public ListClassesByEtudiantWithStatsUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String etudiantId, Callback<List<ClasseWithAttendance>> callback) {
        if (etudiantId == null || etudiantId.isEmpty()) {
            callback.onError(new IllegalStateException("Étudiant non authentifié"));
            return;
        }
        classeRepository.listClassesByEtudiantWithStats(etudiantId, callback);
    }
}
