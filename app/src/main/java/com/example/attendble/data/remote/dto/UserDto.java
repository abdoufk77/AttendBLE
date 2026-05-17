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
    public String department;     // PROFESSEUR
    public String numEtud;        // ETUDIANT
    public float[] faceEmbedding; // ETUDIANT (template MobileFaceNet 192 floats)

    public User toDomain() {
        if (role == UserRole.PROFESSEUR) {
            return new Professeur(uid, email, nom, photoUrl, department);
        }
        Etudiant e = new Etudiant(uid, email, nom, photoUrl, numEtud);
        if (faceEmbedding != null && faceEmbedding.length > 0) {
            e.setFaceEmbedding(faceEmbedding);
        }
        return e;
    }
}
