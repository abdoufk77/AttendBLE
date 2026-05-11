package com.example.attendble.domain.model;

import com.example.attendble.domain.enums.UserRole;

/** Professeur : peut créer des classes et ouvrir des sessions BLE. */
public class Professeur extends User {

    private String department;

    public Professeur() {
    }

    public Professeur(String uid, String email, String nom, String photoUrl, String department) {
        super(uid, email, nom, photoUrl);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public UserRole getRole() {
        return UserRole.PROFESSEUR;
    }
}