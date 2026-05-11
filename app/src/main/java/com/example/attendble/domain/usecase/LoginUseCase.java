package com.example.attendble.domain.usecase;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.User;
import com.example.attendble.domain.repository.AuthRepository;

/** Use case : valide les entrées puis délègue au repository pour authentifier l'utilisateur. */
public class LoginUseCase {

    private final AuthRepository authRepository;

    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(String email, String password, Callback<User> callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Email requis"));
            return;
        }
        if (password == null || password.isEmpty()) {
            callback.onError(new IllegalArgumentException("Mot de passe requis"));
            return;
        }
        authRepository.login(email.trim(), password, callback);
    }
}
