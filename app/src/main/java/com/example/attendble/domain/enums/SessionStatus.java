package com.example.attendble.domain.enums;

/** État d'une session BLE : ACTIVE pendant la diffusion, FERMEE après clôture par le prof. */
public enum SessionStatus {
    ACTIVE,
    FERMEE;

    public static SessionStatus fromString(String value) {
        if (value == null) return null;
        try {
            return SessionStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
