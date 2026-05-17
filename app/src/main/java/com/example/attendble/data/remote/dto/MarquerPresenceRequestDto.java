package com.example.attendble.data.remote.dto;

// Aligné sur com.emsi.attendble.dto.MarquerPresenceRequest côté backend.
public class MarquerPresenceRequestDto {
    public String sessionId;
    public String codeTemp;
    public boolean bleDetecte;
    public boolean faceVerified;

    public MarquerPresenceRequestDto(String sessionId, String codeTemp,
                                     boolean bleDetecte, boolean faceVerified) {
        this.sessionId = sessionId;
        this.codeTemp = codeTemp;
        this.bleDetecte = bleDetecte;
        this.faceVerified = faceVerified;
    }
}
