package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.Classe;

public class CreateClasseRequestDto {
    public String nom;
    public String matiere;
    public String groupe;
    public String salle;
    public String horaire;
    public int jourSemaine;
    public String heureDebut;
    public String heureFin;

    public static CreateClasseRequestDto from(Classe c) {
        CreateClasseRequestDto r = new CreateClasseRequestDto();
        r.nom = c.getNom();
        r.matiere = c.getMatiere();
        r.groupe = c.getGroupe();
        r.salle = c.getSalle();
        r.horaire = c.getHoraire();
        r.jourSemaine = c.getJourSemaine();
        r.heureDebut = c.getHeureDebut();
        r.heureFin = c.getHeureFin();
        return r;
    }
}
