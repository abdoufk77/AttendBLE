package com.example.attendble.data;

import com.example.attendble.data.repository.InMemoryAuthRepository;
import com.example.attendble.domain.repository.AuthRepository;
import com.example.attendble.domain.usecase.LoginUseCase;
import com.example.attendble.domain.usecase.SignupUseCase;

/**
 * Point d'injection unique. L'UI passe par ici pour obtenir use cases / repositories.
 * Pour brancher Firebase plus tard : remplacer {@code new InMemoryAuthRepository()}
 * par {@code new FirebaseAuthRepository()} — rien d'autre à changer.
 */
public final class ServiceLocator {

    private static AuthRepository authRepository;

    private ServiceLocator() {
    }

    public static synchronized AuthRepository getAuthRepository() {
        if (authRepository == null) {
            authRepository = new InMemoryAuthRepository();
        }
        return authRepository;
    }

    public static LoginUseCase provideLoginUseCase() {
        return new LoginUseCase(getAuthRepository());
    }

    public static SignupUseCase provideSignupUseCase() {
        return new SignupUseCase(getAuthRepository());
    }
}
