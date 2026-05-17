package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.Session;

public class OpenSessionRequestDto {
    public String classeId;
    public String codeTemp;
    public String beaconUUID;
    public String codeExpireAt; // ISO-8601 UTC

    public static OpenSessionRequestDto from(Session s) {
        OpenSessionRequestDto dto = new OpenSessionRequestDto();
        dto.classeId = s.getClasseId();
        dto.codeTemp = s.getCodeTemp();
        dto.beaconUUID = s.getBeaconUUID();
        dto.codeExpireAt = java.time.Instant.ofEpochMilli(s.getCodeExpireAt()).toString();
        return dto;
    }
}
