package com.example.attendble.domain.model;

import com.example.attendble.domain.enums.UserRole;

/** Étudiant : rejoint une classe via code d'invitation et confirme sa présence (BLE + code). */
public class Etudiant extends User {

    private String numEtud;

    public Etudiant() {
    }

    public Etudiant(String uid, String email, String nom, String photoUrl, String numEtud) {
        super(uid, email, nom, photoUrl);
        this.numEtud = numEtud;
    }

    public String getNumEtud() {
        return numEtud;
    }

    public void setNumEtud(String numEtud) {
        this.numEtud = numEtud;
    }

    @Override
    public UserRole getRole() {
        return UserRole.ETUDIANT;
    }
}