package com.example.attendble.domain.model;

import com.example.attendble.domain.enums.SessionStatus;

/**
 * Session BLE ouverte par un prof pour une classe.
 * Une classe peut avoir plusieurs sessions (historique). Une seule session ACTIVE à la fois.
 */
public class Session {

    private String sessionId;
    private String classeId;
    private String codeTemp;
    private long codeExpireAt;
    private String beaconUUID;
    private SessionStatus statut;
    private long dateOuverture;
    private Long dateFermeture;

    public Session() {
    }

    public Session(String sessionId, String classeId, String codeTemp, long codeExpireAt,
                   String beaconUUID, SessionStatus statut, long dateOuverture, Long dateFermeture) {
        this.sessionId = sessionId;
        this.classeId = classeId;
        this.codeTemp = codeTemp;
        this.codeExpireAt = codeExpireAt;
        this.beaconUUID = beaconUUID;
        this.statut = statut;
        this.dateOuverture = dateOuverture;
        this.dateFermeture = dateFermeture;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }

    public String getCodeTemp() { return codeTemp; }
    public void setCodeTemp(String codeTemp) { this.codeTemp = codeTemp; }

    public long getCodeExpireAt() { return codeExpireAt; }
    public void setCodeExpireAt(long codeExpireAt) { this.codeExpireAt = codeExpireAt; }

    public String getBeaconUUID() { return beaconUUID; }
    public void setBeaconUUID(String beaconUUID) { this.beaconUUID = beaconUUID; }

    public SessionStatus getStatut() { return statut; }
    public void setStatut(SessionStatus statut) { this.statut = statut; }

    public long getDateOuverture() { return dateOuverture; }
    public void setDateOuverture(long dateOuverture) { this.dateOuverture = dateOuverture; }

    public Long getDateFermeture() { return dateFermeture; }
    public void setDateFermeture(Long dateFermeture) { this.dateFermeture = dateFermeture; }
}
