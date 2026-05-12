package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

/**
 * Use case de création d'une classe : valide les entrées puis délègue au repository
 * (qui génère un {@code classeId} + {@code codeInvitation} unique).
 */
public class CreerClasseUseCase {

    private final ClasseRepository classeRepository;

    public CreerClasseUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String nom, String matiere, String groupe, String salle, String horaire,
                        String nbEtudiantsRaw, String professeurId, Callback<Classe> callback) {
        if (isBlank(nom)) { callback.onError(new IllegalArgumentException("Nom du cours requis")); return; }
        if (isBlank(matiere)) { callback.onError(new IllegalArgumentException("Matière requise")); return; }
        if (isBlank(groupe)) { callback.onError(new IllegalArgumentException("Groupe requis")); return; }
        if (isBlank(salle)) { callback.onError(new IllegalArgumentException("Salle requise")); return; }
        if (isBlank(horaire)) { callback.onError(new IllegalArgumentException("Horaire requis")); return; }
        if (isBlank(nbEtudiantsRaw)) { callback.onError(new IllegalArgumentException("Nombre d'étudiants requis")); return; }
        if (professeurId == null || professeurId.isEmpty()) {
            callback.onError(new IllegalStateException("Professeur non authentifié"));
            return;
        }

        int nbEtudiants;
        try {
            nbEtudiants = Integer.parseInt(nbEtudiantsRaw.trim());
            if (nbEtudiants < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            callback.onError(new IllegalArgumentException("Nombre d'étudiants invalide"));
            return;
        }

        Classe classe = new Classe(
                null, nom.trim(), matiere.trim(), groupe.trim(),
                salle.trim(), horaire.trim(), nbEtudiants, null, professeurId);
        classeRepository.creerClasse(classe, callback);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
