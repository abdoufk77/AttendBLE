package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.enums.UserRole;

// Miroir de com.emsi.attendble.dto.SignupRequest côté backend.
public class SignupRequestDto {
    public String email;
    public String password;
    public String nom;
    public UserRole role;
    public String department; // PROFESSEUR uniquement
    public String numEtud;    // ETUDIANT uniquement
}
