package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.enums.SessionStatus;
import com.example.attendble.domain.model.Session;

// Miroir de com.emsi.attendble.dto.SessionResponse. Instants ISO-8601 (gérés par Gson en String,
// puis convertis en epoch millis via Instant.parse).
public class SessionDto {
    public String sessionId;
    public String classeId;
    public String codeTemp;
    public String codeExpireAt;
    public String beaconUUID;
    public SessionStatus statut;
    public String dateOuverture;
    public String dateFermeture;

    public Session toDomain() {
        return new Session(
                sessionId, classeId, codeTemp,
                parseEpochMillis(codeExpireAt),
                beaconUUID, statut,
                parseEpochMillis(dateOuverture),
                dateFermeture == null ? null : parseEpochMillis(dateFermeture));
    }

    private static long parseEpochMillis(String iso) {
        if (iso == null) return 0L;
        return java.time.Instant.parse(iso).toEpochMilli();
    }
}
