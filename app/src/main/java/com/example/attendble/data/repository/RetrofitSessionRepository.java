package com.example.attendble.data.repository;

import com.example.attendble.data.remote.AttendBleApi;
import com.example.attendble.data.remote.dto.OpenSessionRequestDto;
import com.example.attendble.data.remote.dto.RefreshCodeRequestDto;
import com.example.attendble.data.remote.dto.SessionDto;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.SessionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class RetrofitSessionRepository implements SessionRepository {

    private final AttendBleApi api;

    public RetrofitSessionRepository(AttendBleApi api) {
        this.api = api;
    }

    @Override
    public void ouvrirSession(Session session, Callback<Session> callback) {
        enqueueOne(api.openSession(OpenSessionRequestDto.from(session)),
                SessionDto::toDomain, callback, "Ouverture de session refusée");
    }

    @Override
    public void refreshCode(String sessionId, String nouveauCode, long expireAt, Callback<Session> callback) {
        enqueueOne(api.refreshSessionCode(sessionId, new RefreshCodeRequestDto(nouveauCode, expireAt)),
                SessionDto::toDomain, callback, "Refresh du code refusé");
    }

    @Override
    public void fermerSession(String sessionId, Callback<Session> callback) {
        enqueueOne(api.closeSession(sessionId), SessionDto::toDomain, callback,
                "Fermeture de session refusée");
    }

    @Override
    public void getActiveByClasse(String classeId, Callback<Session> callback) {
        enqueueOne(api.getActiveSession(classeId), SessionDto::toDomain, callback,
                "Aucune session active");
    }

    @Override
    public void findById(String sessionId, Callback<Session> callback) {
        enqueueOne(api.getSession(sessionId), SessionDto::toDomain, callback, "Session introuvable");
    }

    @Override
    public void listByClasse(String classeId, Callback<List<Session>> callback) {
        enqueueList(api.listSessionsByClasse(classeId), SessionDto::toDomain, callback,
                "Impossible de lister les sessions");
    }

    @Override
    public void listAllActive(Callback<List<Session>> callback) {
        enqueueList(api.listAllActiveSessions(), SessionDto::toDomain, callback,
                "Impossible de lister les sessions actives");
    }

    // --- helpers (identiques à RetrofitClasseRepository — duplication acceptée pour rester simple) ---

    private <Dto, Domain> void enqueueList(Call<List<Dto>> call,
                                           java.util.function.Function<Dto, Domain> mapper,
                                           Callback<List<Domain>> callback,
                                           String fallback) {
        call.enqueue(new retrofit2.Callback<List<Dto>>() {
            @Override
            public void onResponse(Call<List<Dto>> c, Response<List<Dto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, fallback)));
                    return;
                }
                List<Domain> out = new ArrayList<>(response.body().size());
                for (Dto d : response.body()) out.add(mapper.apply(d));
                callback.onSuccess(out);
            }

            @Override
            public void onFailure(Call<List<Dto>> c, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    private <Dto, Domain> void enqueueOne(Call<Dto> call,
                                          java.util.function.Function<Dto, Domain> mapper,
                                          Callback<Domain> callback,
                                          String fallback) {
        call.enqueue(new retrofit2.Callback<Dto>() {
            @Override
            public void onResponse(Call<Dto> c, Response<Dto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, fallback)));
                    return;
                }
                callback.onSuccess(mapper.apply(response.body()));
            }

            @Override
            public void onFailure(Call<Dto> c, Throwable t) {
                callback.onError(asException(t));
            }
        });
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
