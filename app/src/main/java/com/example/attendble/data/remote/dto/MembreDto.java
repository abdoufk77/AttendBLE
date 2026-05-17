package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.Etudiant;

// Réponse simplifiée d'un étudiant inscrit (sans stats ni photo/embedding).
public class MembreDto {
    public String etudiantId;
    public String nom;
    public String email;
    public String numEtud;

    public Etudiant toDomain() {
        return new Etudiant(etudiantId, email, nom, null, numEtud);
    }
}
