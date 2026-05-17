package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.Classe;

// Miroir de com.emsi.attendble.dto.ClasseResponse côté backend.
public class ClasseDto {
    public String classeId;
    public String nom;
    public String matiere;
    public String groupe;
    public String salle;
    public String horaire;
    public int jourSemaine;
    public String heureDebut;
    public String heureFin;
    public int nbEtudiants;
    public String codeInvitation;
    public String professeurId;

    public Classe toDomain() {
        return new Classe(classeId, nom, matiere, groupe, salle, horaire,
                jourSemaine, heureDebut, heureFin, nbEtudiants, codeInvitation, professeurId);
    }
}
