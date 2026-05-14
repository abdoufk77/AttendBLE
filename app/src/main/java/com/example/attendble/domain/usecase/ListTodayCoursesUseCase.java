package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.repository.ClasseRepository;

import java.util.Calendar;
import java.util.List;

/**
 * Liste les cours du jour pour un utilisateur (prof ou étudiant) selon son rôle.
 * Le {@code jourSemaine} ISO (1=Lundi..7=Dimanche) est calculé depuis l'horloge système.
 */
public class ListTodayCoursesUseCase {

    private final ClasseRepository classeRepository;

    public ListTodayCoursesUseCase(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public void execute(String userId, UserRole role, Callback<List<Classe>> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError(new IllegalStateException("Utilisateur non authentifié"));
            return;
        }
        int jourIso = todayIsoDayOfWeek();
        if (role == UserRole.PROFESSEUR) {
            classeRepository.listTodayByProfesseur(userId, jourIso, callback);
        } else {
            classeRepository.listTodayByEtudiant(userId, jourIso, callback);
        }
    }

    /** Convertit Calendar.DAY_OF_WEEK (Dim=1..Sam=7) en ISO (Lun=1..Dim=7). */
    public static int todayIsoDayOfWeek() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return dow == Calendar.SUNDAY ? 7 : dow - 1;
    }
}
