package com.example.attendble.domain.model;

/**
 * Stats globales d'un professeur (affichées sur le Home prof).
 * {@code totalStudents} = nb d'étudiants distincts inscrits dans une classe du prof.
 * {@code avgAttendance} = % de présence moyen sur toutes les sessions FERMEE de ses classes
 * (100 si aucune session encore tenue).
 */
public class ProfStats {

    private final int totalStudents;
    private final int avgAttendance;
    private final int totalClasses;

    public ProfStats(int totalStudents, int avgAttendance, int totalClasses) {
        this.totalStudents = totalStudents;
        this.avgAttendance = avgAttendance;
        this.totalClasses = totalClasses;
    }

    public int getTotalStudents() { return totalStudents; }
    public int getAvgAttendance() { return avgAttendance; }
    public int getTotalClasses() { return totalClasses; }
}
