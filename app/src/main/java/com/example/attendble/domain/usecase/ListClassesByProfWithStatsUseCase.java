package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.repository.ClasseRepository;

import java.util.List;

/** Classes du prof + taux de présence moyen par classe. */
public class ListClassesByProfWithStatsUseCase {

    private final ClasseRepository classeRepository;

    public ListClassesByProfWithStatsUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String professeurId, Callback<List<ClasseWithAttendance>> callback) {
        if (professeurId == null || professeurId.isEmpty()) {
            callback.onError(new IllegalStateException("Prof non authentifié"));
            return;
        }
        classeRepository.listClassesByProfesseurWithStats(professeurId, callback);
    }
}
