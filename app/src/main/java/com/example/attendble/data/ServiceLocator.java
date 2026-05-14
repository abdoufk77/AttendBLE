package com.example.attendble.data;

import android.content.Context;

import com.example.attendble.data.local.AttendBleDbHelper;
import com.example.attendble.data.repository.SqliteAuthRepository;
import com.example.attendble.data.repository.SqliteClasseRepository;
import com.example.attendble.data.repository.SqlitePointageRepository;
import com.example.attendble.data.repository.SqliteSessionRepository;
import com.example.attendble.domain.repository.AuthRepository;
import com.example.attendble.domain.repository.ClasseRepository;
import com.example.attendble.domain.repository.PointageRepository;
import com.example.attendble.domain.repository.SessionRepository;
import com.example.attendble.domain.usecase.CreerClasseUseCase;
import com.example.attendble.domain.usecase.FermerSessionUseCase;
import com.example.attendble.domain.usecase.GetClasseUseCase;
import com.example.attendble.domain.usecase.JoinClasseUseCase;
import com.example.attendble.domain.usecase.ListClassesByEtudiantUseCase;
import com.example.attendble.domain.usecase.ListEtudiantsByClasseUseCase;
import com.example.attendble.domain.usecase.ListClassesByProfUseCase;
import com.example.attendble.domain.usecase.ListPointagesBySessionUseCase;
import com.example.attendble.domain.usecase.ListSessionsByClasseUseCase;
import com.example.attendble.domain.usecase.ListTodayCoursesUseCase;
import com.example.attendble.domain.usecase.LoginUseCase;
import com.example.attendble.domain.usecase.OuvrirSessionUseCase;
import com.example.attendble.domain.usecase.RefreshCodeUseCase;
import com.example.attendble.domain.usecase.SignupUseCase;
import com.example.attendble.domain.usecase.ValiderPresenceUseCase;

/**
 * Point d'injection unique. {@link #init(Context)} doit être appelé au démarrage
 * (depuis {@code AttendBleApp.onCreate()}) car SQLite a besoin du contexte d'application.
 * Pour brancher Retrofit/Spring plus tard : remplacer les {@code SqliteXxxRepository}
 * par leur équivalent réseau — rien d'autre à changer dans l'UI.
 */
public final class ServiceLocator {

    private static AuthRepository authRepository;
    private static ClasseRepository classeRepository;
    private static SessionRepository sessionRepository;
    private static PointageRepository pointageRepository;

    private ServiceLocator() {
    }

    public static synchronized void init(Context context) {
        if (authRepository != null) return;
        AttendBleDbHelper helper = AttendBleDbHelper.getInstance(context);
        authRepository = new SqliteAuthRepository(helper);
        classeRepository = new SqliteClasseRepository(helper);
        sessionRepository = new SqliteSessionRepository(helper);
        pointageRepository = new SqlitePointageRepository(helper);
    }

    public static AuthRepository getAuthRepository() {
        ensureInit();
        return authRepository;
    }

    public static ClasseRepository getClasseRepository() {
        ensureInit();
        return classeRepository;
    }

    public static SessionRepository getSessionRepository() {
        ensureInit();
        return sessionRepository;
    }

    public static PointageRepository getPointageRepository() {
        ensureInit();
        return pointageRepository;
    }

    public static LoginUseCase provideLoginUseCase() {
        return new LoginUseCase(getAuthRepository());
    }

    public static SignupUseCase provideSignupUseCase() {
        return new SignupUseCase(getAuthRepository());
    }

    public static CreerClasseUseCase provideCreerClasseUseCase() {
        return new CreerClasseUseCase(getClasseRepository());
    }

    public static ListClassesByProfUseCase provideListClassesByProfUseCase() {
        return new ListClassesByProfUseCase(getClasseRepository());
    }

    public static GetClasseUseCase provideGetClasseUseCase() {
        return new GetClasseUseCase(getClasseRepository());
    }

    public static JoinClasseUseCase provideJoinClasseUseCase() {
        return new JoinClasseUseCase(getClasseRepository());
    }

    public static ListClassesByEtudiantUseCase provideListClassesByEtudiantUseCase() {
        return new ListClassesByEtudiantUseCase(getClasseRepository());
    }

    public static ListEtudiantsByClasseUseCase provideListEtudiantsByClasseUseCase() {
        return new ListEtudiantsByClasseUseCase(getClasseRepository());
    }

    public static OuvrirSessionUseCase provideOuvrirSessionUseCase() {
        return new OuvrirSessionUseCase(getSessionRepository());
    }

    public static RefreshCodeUseCase provideRefreshCodeUseCase() {
        return new RefreshCodeUseCase(getSessionRepository());
    }

    public static FermerSessionUseCase provideFermerSessionUseCase() {
        return new FermerSessionUseCase(getSessionRepository());
    }

    public static ValiderPresenceUseCase provideValiderPresenceUseCase() {
        return new ValiderPresenceUseCase(getSessionRepository(), getPointageRepository());
    }

    public static ListPointagesBySessionUseCase provideListPointagesBySessionUseCase() {
        return new ListPointagesBySessionUseCase(getPointageRepository());
    }

    public static ListSessionsByClasseUseCase provideListSessionsByClasseUseCase() {
        return new ListSessionsByClasseUseCase(getSessionRepository());
    }

    public static ListTodayCoursesUseCase provideListTodayCoursesUseCase() {
        return new ListTodayCoursesUseCase(getClasseRepository());
    }

    private static void ensureInit() {
        if (authRepository == null) {
            throw new IllegalStateException("ServiceLocator non initialisé — appeler ServiceLocator.init(context) depuis Application.onCreate()");
        }
    }
}
