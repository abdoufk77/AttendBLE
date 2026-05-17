package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.model.User;

// Miroir de com.emsi.attendble.dto.UserResponse côté backend (GET /api/users/me).
public class UserDto {
    public String uid;
    public String email;
    public String nom;
    public String photoUrl;
    public UserRole role;
    public String department; // PROFESSEUR
    public String numEtud;    // ETUDIANT

    public User toDomain() {
        if (role == UserRole.PROFESSEUR) {
            return new Professeur(uid, email, nom, photoUrl, department);
        }
        return new Etudiant(uid, email, nom, photoUrl, numEtud);
    }
}
