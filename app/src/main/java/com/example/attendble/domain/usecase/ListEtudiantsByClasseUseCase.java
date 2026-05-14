package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.EtudiantAttendance;
import com.example.attendble.domain.repository.ClasseRepository;

import java.util.List;

/** Liste les étudiants inscrits à une classe (avec leurs stats de présence). */
public class ListEtudiantsByClasseUseCase {

    private final ClasseRepository classeRepository;

    public ListEtudiantsByClasseUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String classeId, Callback<List<EtudiantAttendance>> callback) {
        if (classeId == null || classeId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Classe requise"));
            return;
        }
        classeRepository.listEtudiantsByClasse(classeId, callback);
    }
}
