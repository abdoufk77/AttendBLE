package com.example.attendble.domain.model;

import com.example.attendble.domain.enums.PointageStatut;

/**
 * Enregistrement de présence d'un étudiant à une session.
 * Triple preuve : bleDetecte (signal BLE), codeValide implicite (validation à l'insertion), faceVerified.
 */
public class Pointage {

    private String pointageId;
    private String sessionId;
    private String etudiantId;
    private long heurePointage;
    private PointageStatut statut;
    private boolean bleDetecte;
    private boolean faceVerified;

    public Pointage() {
    }

    public Pointage(String pointageId, String sessionId, String etudiantId, long heurePointage,
                    PointageStatut statut, boolean bleDetecte, boolean faceVerified) {
        this.pointageId = pointageId;
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.heurePointage = heurePointage;
        this.statut = statut;
        this.bleDetecte = bleDetecte;
        this.faceVerified = faceVerified;
    }

    public String getPointageId() { return pointageId; }
    public void setPointageId(String pointageId) { this.pointageId = pointageId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getEtudiantId() { return etudiantId; }
    public void setEtudiantId(String etudiantId) { this.etudiantId = etudiantId; }

    public long getHeurePointage() { return heurePointage; }
    public void setHeurePointage(long heurePointage) { this.heurePointage = heurePointage; }

    public PointageStatut getStatut() { return statut; }
    public void setStatut(PointageStatut statut) { this.statut = statut; }

    public boolean isBleDetecte() { return bleDetecte; }
    public void setBleDetecte(boolean bleDetecte) { this.bleDetecte = bleDetecte; }

    public boolean isFaceVerified() { return faceVerified; }
    public void setFaceVerified(boolean faceVerified) { this.faceVerified = faceVerified; }
}
