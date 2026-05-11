package com.example.attendble.domain.enums;

/**
 * Rôle d'un User. Stocké côté Firebase RTDB comme String via {@link #name()}
 * sous /users/{uid}/role — utilisé par les règles de sécurité RTDB pour autoriser
 * la création de classes/sessions uniquement aux professeurs.
 */
public enum UserRole {
    PROFESSEUR,
    ETUDIANT;

    public static UserRole fromString(String value) {
        if (value == null) return null;
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}