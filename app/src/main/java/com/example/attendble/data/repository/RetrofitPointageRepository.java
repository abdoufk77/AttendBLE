package com.example.attendble.data.repository;

import com.example.attendble.data.remote.AttendBleApi;
import com.example.attendble.data.remote.dto.MarquerPresenceRequestDto;
import com.example.attendble.data.remote.dto.PointageDto;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Pointage;
import com.example.attendble.domain.repository.PointageRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class RetrofitPointageRepository implements PointageRepository {

    private final AttendBleApi api;

    public RetrofitPointageRepository(AttendBleApi api) {
        this.api = api;
    }

    @Override
    public void validerPresence(Pointage pointage, String codeSaisi, Callback<Pointage> callback) {
        MarquerPresenceRequestDto body = new MarquerPresenceRequestDto(
                pointage.getSessionId(), codeSaisi, pointage.isBleDetecte(), pointage.isFaceVerified());
        api.marquerPresence(body).enqueue(new retrofit2.Callback<PointageDto>() {
            @Override
            public void onResponse(Call<PointageDto> c, Response<PointageDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Validation refusée")));
                    return;
                }
                callback.onSuccess(response.body().toDomain());
            }

            @Override
            public void onFailure(Call<PointageDto> c, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    @Override
    public void listBySession(String sessionId, Callback<List<Pointage>> callback) {
        api.listPointagesBySession(sessionId).enqueue(new retrofit2.Callback<List<PointageDto>>() {
            @Override
            public void onResponse(Call<List<PointageDto>> c, Response<List<PointageDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Impossible de lister")));
                    return;
                }
                List<Pointage> out = new ArrayList<>(response.body().size());
                for (PointageDto d : response.body()) out.add(d.toDomain());
                callback.onSuccess(out);
            }

            @Override
            public void onFailure(Call<List<PointageDto>> c, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    @Override
    public void listByEtudiant(String etudiantId, Callback<List<Pointage>> callback) {
        api.listMyPointages().enqueue(new retrofit2.Callback<List<PointageDto>>() {
            @Override
            public void onResponse(Call<List<PointageDto>> c, Response<List<PointageDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Impossible de lister")));
                    return;
                }
                List<Pointage> out = new ArrayList<>(response.body().size());
                for (PointageDto d : response.body()) out.add(d.toDomain());
                callback.onSuccess(out);
            }

            @Override
            public void onFailure(Call<List<PointageDto>> c, Throwable t) {
                callback.onError(asException(t));
            }
        });
    }

    @Override
    public void hasPointage(String sessionId, String etudiantId, Callback<Boolean> callback) {
        api.checkHasPointage(sessionId).enqueue(new retrofit2.Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> c, Response<Map<String, Boolean>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new Exception(httpErrorMessage(response, "Vérification échouée")));
                    return;
                }
                Boolean v = response.body().get("hasPointage");
                callback.onSuccess(v != null && v);
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> c, Throwable t) {
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
