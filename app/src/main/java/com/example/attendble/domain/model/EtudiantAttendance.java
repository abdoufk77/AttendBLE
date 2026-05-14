package com.example.attendble.domain.model;

/**
 * Étudiant inscrit à une classe avec ses statistiques de présence sur cette classe.
 * {@code nbSessionsFermees} = sessions FERMEE de la classe ; {@code nbPresences} = pointages PRESENT.
 * Un taux de 100% est convenu quand aucune session n'a encore été tenue.
 */
public class EtudiantAttendance {

    private final Etudiant etudiant;
    private final int nbPresences;
    private final int nbSessionsFermees;

    public EtudiantAttendance(Etudiant etudiant, int nbPresences, int nbSessionsFermees) {
        this.etudiant = etudiant;
        this.nbPresences = nbPresences;
        this.nbSessionsFermees = nbSessionsFermees;
    }

    public Etudiant getEtudiant() { return etudiant; }
    public int getNbPresences() { return nbPresences; }
    public int getNbSessionsFermees() { return nbSessionsFermees; }

    /** Taux de présence en pourcentage [0..100]. 100 si aucune session encore tenue. */
    public int getTauxPresence() {
        if (nbSessionsFermees == 0) return 100;
        return Math.round(100f * nbPresences / nbSessionsFermees);
    }
}
