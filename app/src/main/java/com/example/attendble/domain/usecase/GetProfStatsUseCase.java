package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.ProfStats;
import com.example.attendble.domain.repository.ClasseRepository;

/** Stats globales du prof : total étudiants distincts + taux moyen de présence. */
public class GetProfStatsUseCase {

    private final ClasseRepository classeRepository;

    public GetProfStatsUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String professeurId, Callback<ProfStats> callback) {
        if (professeurId == null || professeurId.isEmpty()) {
            callback.onError(new IllegalStateException("Prof non authentifié"));
            return;
        }
        classeRepository.getProfStats(professeurId, callback);
    }
}
