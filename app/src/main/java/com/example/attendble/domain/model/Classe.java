package com.example.attendble.domain.model;

/**
 * POJO Classe : un cours géré par un professeur, rejoignable via {@code codeInvitation}.
 * Constructeur vide + getters/setters pour compatibilité Firebase RTDB future.
 */
public class Classe {

    private String classeId;
    private String nom;
    private String matiere;
    private String groupe;
    private String salle;
    private String horaire;
    private int nbEtudiants;
    private String codeInvitation;
    private String professeurId;

    public Classe() {
    }

    public Classe(String classeId, String nom, String matiere, String groupe, String salle,
                  String horaire, int nbEtudiants, String codeInvitation, String professeurId) {
        this.classeId = classeId;
        this.nom = nom;
        this.matiere = matiere;
        this.groupe = groupe;
        this.salle = salle;
        this.horaire = horaire;
        this.nbEtudiants = nbEtudiants;
        this.codeInvitation = codeInvitation;
        this.professeurId = professeurId;
    }

    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public String getHoraire() { return horaire; }
    public void setHoraire(String horaire) { this.horaire = horaire; }

    public int getNbEtudiants() { return nbEtudiants; }
    public void setNbEtudiants(int nbEtudiants) { this.nbEtudiants = nbEtudiants; }

    public String getCodeInvitation() { return codeInvitation; }
    public void setCodeInvitation(String codeInvitation) { this.codeInvitation = codeInvitation; }

    public String getProfesseurId() { return professeurId; }
    public void setProfesseurId(String professeurId) { this.professeurId = professeurId; }
}
