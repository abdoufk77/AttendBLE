package com.example.attendble.data.remote.dto;

// Miroir de com.emsi.attendble.dto.LoginRequest côté backend.
public class LoginRequestDto {
    public String email;
    public String password;

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
