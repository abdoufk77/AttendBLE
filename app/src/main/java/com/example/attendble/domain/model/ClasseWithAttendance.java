package com.example.attendble.domain.model;

/**
 * Classe + taux de présence (selon le contexte : moyenne des étudiants côté prof,
 * propre taux côté étudiant). {@code nbSessionsFermees} sert à l'UI pour afficher
 * "Aucune session" quand 0.
 */
public class ClasseWithAttendance {

    private final Classe classe;
    private final int tauxPresence;
    private final int nbSessionsFermees;

    public ClasseWithAttendance(Classe classe, int tauxPresence, int nbSessionsFermees) {
        this.classe = classe;
        this.tauxPresence = tauxPresence;
        this.nbSessionsFermees = nbSessionsFermees;
    }

    public Classe getClasse() { return classe; }
    public int getTauxPresence() { return tauxPresence; }
    public int getNbSessionsFermees() { return nbSessionsFermees; }
}
