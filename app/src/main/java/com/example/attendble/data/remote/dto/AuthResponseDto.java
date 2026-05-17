package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.enums.UserRole;

// Réponse renvoyée par /api/auth/login et /api/auth/signup.
public class AuthResponseDto {
    public String token;
    public String uid;
    public String email;
    public String nom;
    public UserRole role;
}
