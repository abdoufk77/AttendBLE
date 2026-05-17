package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.ClasseWithAttendance;

public class ClasseWithAttendanceDto {
    public ClasseDto classe;
    public int tauxPresence;
    public int nbSessionsFermees;

    public ClasseWithAttendance toDomain() {
        return new ClasseWithAttendance(classe.toDomain(), tauxPresence, nbSessionsFermees);
    }
}
