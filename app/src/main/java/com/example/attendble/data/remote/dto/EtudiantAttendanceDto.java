package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.EtudiantAttendance;

// Miroir de com.emsi.attendble.dto.EtudiantAttendanceResponse côté backend.
public class EtudiantAttendanceDto {
    public String etudiantId;
    public String nom;
    public String email;
    public String numEtud;
    public int nbPresences;
    public int nbSessionsFermees;
    public int tauxPresence; // calculé côté backend, ignoré ici (recalculé par le domaine)

    public EtudiantAttendance toDomain() {
        Etudiant e = new Etudiant(etudiantId, email, nom, null, numEtud);
        return new EtudiantAttendance(e, nbPresences, nbSessionsFermees);
    }
}
