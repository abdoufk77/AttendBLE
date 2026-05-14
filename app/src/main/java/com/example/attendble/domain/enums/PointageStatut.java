package com.example.attendble.domain.enums;

/** Statut d'un pointage : PRESENT si validé pendant la session, ABSENT sinon (auto à la clôture). */
public enum PointageStatut {
    PRESENT,
    ABSENT;

    public static PointageStatut fromString(String value) {
        if (value == null) return null;
        try {
            return PointageStatut.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
