package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.enums.PointageStatut;
import com.example.attendble.domain.model.Pointage;

public class PointageDto {
    public String pointageId;
    public String sessionId;
    public String etudiantId;
    public String heurePointage; // ISO-8601
    public PointageStatut statut;
    public boolean bleDetecte;
    public boolean faceVerified;

    public Pointage toDomain() {
        long heure = heurePointage == null ? 0L : java.time.Instant.parse(heurePointage).toEpochMilli();
        return new Pointage(pointageId, sessionId, etudiantId, heure,
                statut, bleDetecte, faceVerified);
    }
}
