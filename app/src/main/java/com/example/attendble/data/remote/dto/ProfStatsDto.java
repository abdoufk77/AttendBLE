package com.example.attendble.data.remote.dto;

import com.example.attendble.domain.model.ProfStats;

public class ProfStatsDto {
    public int totalStudents;
    public int avgAttendance;
    public int totalClasses;

    public ProfStats toDomain() {
        return new ProfStats(totalStudents, avgAttendance, totalClasses);
    }
}
