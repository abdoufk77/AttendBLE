package com.example.attendble.data.remote.dto;

public class RefreshCodeRequestDto {
    public String codeTemp;
    public String codeExpireAt;

    public RefreshCodeRequestDto(String codeTemp, long codeExpireAtEpochMillis) {
        this.codeTemp = codeTemp;
        this.codeExpireAt = java.time.Instant.ofEpochMilli(codeExpireAtEpochMillis).toString();
    }
}
