package com.example.attendble.data;

import com.example.attendble.data.repository.InMemoryAuthRepository;
import com.example.attendble.data.repository.InMemoryClasseRepository;
import com.example.attendble.domain.repository.AuthRepository;
import com.example.attendble.domain.repository.ClasseRepository;
import com.example.attendble.domain.usecase.CreerClasseUseCase;
import com.example.attendble.domain.usecase.GetClasseUseCase;
import com.example.attendble.domain.usecase.JoinClasseUseCase;
import com.example.attendble.domain.usecase.ListClassesByEtudiantUseCase;
import com.example.attendble.domain.usecase.ListClassesByProfUseCase;
import com.example.attendble.domain.usecase.LoginUseCase;
import com.example.attendble.domain.usecase.SignupUseCase;

/**
 * Point d'injection unique. L'UI passe par ici pour obtenir use cases / repositories.
 * Pour brancher Firebase plus tard : remplacer les {@code InMemoryXxxRepository}
 * par leur équivalent Firebase — rien d'autre à changer.
 */
public final class ServiceLocator {

    private static AuthRepository authRepository;
    private static ClasseRepository classeRepository;

    private ServiceLocator() {
    }

    public static synchronized AuthRepository getAuthRepository() {
        if (authRepository == null) {
            authRepository = new InMemoryAuthRepository();
        }
        return authRepository;
    }

    public static synchronized ClasseRepository getClasseRepository() {
        if (classeRepository == null) {
            classeRepository = new InMemoryClasseRepository();
        }
        return classeRepository;
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
}
