package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.repository.AuthRepository;

/**
 * Use case d'inscription. Deux méthodes distinctes (Professeur/Etudiant) car les
 * champs spécifiques diffèrent (department vs numEtud) et le typage est plus clair pour l'UI.
 */
public class SignupUseCase {

    private final AuthRepository authRepository;

    public SignupUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void signupProfesseur(String email, String password, String nom, String department,
                                 Callback<Professeur> callback) {
        Exception validation = validateCommon(email, password, nom);
        if (validation != null) {
            callback.onError(validation);
            return;
        }
        if (department == null || department.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Département requis"));
            return;
        }
        Professeur professeur = new Professeur(null, email.trim(), nom.trim(), null, department.trim());
        authRepository.signupProfesseur(email.trim(), password, professeur, callback);
    }

    public void signupEtudiant(String email, String password, String nom, String numEtud,
                               Callback<Etudiant> callback) {
        Exception validation = validateCommon(email, password, nom);
        if (validation != null) {
            callback.onError(validation);
            return;
        }
        if (numEtud == null || numEtud.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Numéro étudiant requis"));
            return;
        }
        Etudiant etudiant = new Etudiant(null, email.trim(), nom.trim(), null, numEtud.trim());
        authRepository.signupEtudiant(email.trim(), password, etudiant, callback);
    }

    private Exception validateCommon(String email, String password, String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return new IllegalArgumentException("Nom requis");
        }
        if (email == null || email.trim().isEmpty()) {
            return new IllegalArgumentException("Email requis");
        }
        if (password == null || password.length() < 6) {
            return new IllegalArgumentException("Mot de passe : 6 caractères minimum");
        }
        return null;
    }
}