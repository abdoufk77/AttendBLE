package com.example.attendble.data.repository;

import com.example.attendble.data.remote.AttendBleApi;
import com.example.attendble.data.remote.TokenStore;
import com.example.attendble.data.remote.dto.AuthResponseDto;
import com.example.attendble.data.remote.dto.LoginRequestDto;
import com.example.attendble.data.remote.dto.SignupRequestDto;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.model.User;
import com.example.attendble.domain.repository.AuthRepository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

// Implémentation Retrofit de AuthRepository (backend Spring Boot /api/auth/**).
// Stocke le JWT + uid + role dans TokenStore après login/signup.
public class RetrofitAuthRepository implements AuthRepository {

    private final AttendBleApi api;
    private final TokenStore tokenStore;

    public RetrofitAuthRepository(AttendBleApi api, TokenStore tokenStore) {
        this.api = api;
        this.tokenStore = tokenStore;
    }

    @Override
    public void login(String email, String password, Callback<User> callback) {
        api.login(new LoginRequestDto(email, password)).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Identifiants invalides")));
                    return;
                }
                AuthResponseDto body = response.body();
                tokenStore.save(body.token, body.uid, body.role.name());
                callback.onSuccess(toUser(body));
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    @Override
    public void signupProfesseur(String email, String password, Professeur professeur, Callback<Professeur> callback) {
        SignupRequestDto req = new SignupRequestDto();
        req.email = email;
        req.password = password;
        req.nom = professeur.getNom();
        req.role = UserRole.PROFESSEUR;
        req.department = professeur.getDepartment();
        api.signup(req).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Inscription refusée")));
                    return;
                }
                AuthResponseDto body = response.body();
                tokenStore.save(body.token, body.uid, body.role.name());
                professeur.setUid(body.uid);
                professeur.setEmail(body.email);
                callback.onSuccess(professeur);
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    @Override
    public void signupEtudiant(String email, String password, Etudiant etudiant, Callback<Etudiant> callback) {
        SignupRequestDto req = new SignupRequestDto();
        req.email = email;
        req.password = password;
        req.nom = etudiant.getNom();
        req.role = UserRole.ETUDIANT;
        req.numEtud = etudiant.getNumEtud();
        api.signup(req).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Inscription refusée")));
                    return;
                }
                AuthResponseDto body = response.body();
                tokenStore.save(body.token, body.uid, body.role.name());
                etudiant.setUid(body.uid);
                etudiant.setEmail(body.email);
                callback.onSuccess(etudiant);
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    @Override
    public void logout() {
        tokenStore.clear();
    }

    @Override
    public String getCurrentUserId() {
        return tokenStore.getUid();
    }

    @Override
    public boolean isLoggedIn() {
        return tokenStore.isLoggedIn();
    }

    @Override
    public void getCurrentUser(Callback<User> callback) {
        // Sans endpoint /api/auth/me, on reconstruit un User minimal depuis le TokenStore.
        // À enrichir quand le backend exposera /api/users/me.
        String uid = tokenStore.getUid();
        String role = tokenStore.getRole();
        if (uid == null || role == null) {
            callback.onError(new Exception("Pas d'utilisateur connecté"));
            return;
        }
        User u;
        if (UserRole.PROFESSEUR.name().equals(role)) {
            Professeur p = new Professeur();
            p.setUid(uid);
            u = p;
        } else {
            Etudiant e = new Etudiant();
            e.setUid(uid);
            u = e;
        }
        callback.onSuccess(u);
    }

    @Override
    public void updateFaceEmbedding(String uid, float[] embedding, Callback<Void> callback) {
        // Endpoint backend pas encore exposé (à ajouter : PUT /api/etudiants/me/face-embedding).
        callback.onError(new UnsupportedOperationException(
                "updateFaceEmbedding : endpoint backend pas encore implémenté"));
    }

    // --- helpers ---

    private User toUser(AuthResponseDto dto) {
        if (dto.role == UserRole.PROFESSEUR) {
            Professeur p = new Professeur();
            p.setUid(dto.uid);
            p.setEmail(dto.email);
            p.setNom(dto.nom);
            return p;
        }
        Etudiant e = new Etudiant();
        e.setUid(dto.uid);
        e.setEmail(dto.email);
        e.setNom(dto.nom);
        return e;
    }

    private String httpErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                if (!body.isEmpty()) return body;
            }
        } catch (IOException ignored) {
        }
        return fallback + " (HTTP " + response.code() + ")";
    }

    private Exception asException(Throwable t) {
        return (t instanceof Exception) ? (Exception) t : new Exception(t);
    }
}
