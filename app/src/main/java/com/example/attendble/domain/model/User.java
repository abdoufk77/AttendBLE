package com.example.attendble.domain.model;

import com.example.attendble.domain.enums.UserRole;

/**
 * POJO de base pour un utilisateur authentifié.
 * Pas de champ password : géré par Firebase Auth (jamais stocké dans la DB).
 * Constructeur vide + getters/setters requis pour Firebase RTDB ({@code snapshot.getValue(...)}).
 */
public abstract class User {

    protected String uid;
    protected String email;
    protected String nom;
    protected String photoUrl;

    public User() {
    }

    public User(String uid, String email, String nom, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.nom = nom;
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public abstract UserRole getRole();
}