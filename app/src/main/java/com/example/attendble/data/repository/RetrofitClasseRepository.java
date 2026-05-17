package com.example.attendble.data.repository;

import com.example.attendble.data.remote.AttendBleApi;
import com.example.attendble.data.remote.dto.ClasseDto;
import com.example.attendble.data.remote.dto.ClasseWithAttendanceDto;
import com.example.attendble.data.remote.dto.CreateClasseRequestDto;
import com.example.attendble.data.remote.dto.EtudiantAttendanceDto;
import com.example.attendble.data.remote.dto.JoinClasseRequestDto;
import com.example.attendble.data.remote.dto.MembreDto;
import com.example.attendble.data.remote.dto.ProfStatsDto;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.model.EtudiantAttendance;
import com.example.attendble.domain.model.ProfStats;
import com.example.attendble.domain.repository.ClasseRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

// Implémentation Retrofit de ClasseRepository (backend Spring Boot /api/classes/**).
// Les paramètres d'id (professeurId, etudiantId) sont ignorés : le backend identifie
// l'utilisateur via le JWT.
public class RetrofitClasseRepository implements ClasseRepository {

    private final AttendBleApi api;

    public RetrofitClasseRepository(AttendBleApi api) {
        this.api = api;
    }

    @Override
    public void creerClasse(Classe classe, Callback<Classe> callback) {
        enqueueOne(api.createClasse(CreateClasseRequestDto.from(classe)),
                ClasseDto::toDomain, callback, "Création de classe refusée");
    }

    @Override
    public void listClassesByProfesseur(String professeurId, Callback<List<Classe>> callback) {
        enqueueList(api.listMyClasses(), ClasseDto::toDomain, callback, "Impossible de lister les classes");
    }

    @Override
    public void findById(String classeId, Callback<Classe> callback) {
        enqueueOne(api.findClasseById(classeId), ClasseDto::toDomain, callback, "Classe introuvable");
    }

    @Override
    public void findByCodeInvitation(String codeInvitation, Callback<Classe> callback) {
        enqueueOne(api.findClasseByCode(codeInvitation), ClasseDto::toDomain, callback,
                "Code d'invitation invalide");
    }

    @Override
    public void joinClasseByCode(String codeInvitation, String etudiantId, Callback<Classe> callback) {
        enqueueOne(api.joinClasse(new JoinClasseRequestDto(codeInvitation)),
                ClasseDto::toDomain, callback, "Inscription refusée");
    }

    @Override
    public void listClassesByEtudiant(String etudiantId, Callback<List<Classe>> callback) {
        enqueueList(api.listJoinedClasses(), ClasseDto::toDomain, callback,
                "Impossible de lister les classes");
    }

    @Override
    public void listTodayByProfesseur(String professeurId, int jourSemaine, Callback<List<Classe>> callback) {
        enqueueList(api.listTodayMyClasses(jourSemaine), ClasseDto::toDomain, callback,
                "Impossible de lister les cours du jour");
    }

    @Override
    public void listTodayByEtudiant(String etudiantId, int jourSemaine, Callback<List<Classe>> callback) {
        enqueueList(api.listTodayJoinedClasses(jourSemaine), ClasseDto::toDomain, callback,
                "Impossible de lister les cours du jour");
    }

    @Override
    public void listEtudiantsByClasse(String classeId, Callback<List<EtudiantAttendance>> callback) {
        enqueueList(api.listEtudiantsAttendance(classeId), EtudiantAttendanceDto::toDomain, callback,
                "Impossible de charger les étudiants");
    }

    @Override
    public void getProfStats(String professeurId, Callback<ProfStats> callback) {
        enqueueOne(api.getProfStats(), ProfStatsDto::toDomain, callback, "Impossible de charger les stats");
    }

    @Override
    public void listClassesByProfesseurWithStats(String professeurId, Callback<List<ClasseWithAttendance>> callback) {
        enqueueList(api.listMyClassesWithStats(), ClasseWithAttendanceDto::toDomain, callback,
                "Impossible de charger les classes");
    }

    @Override
    public void listClassesByEtudiantWithStats(String etudiantId, Callback<List<ClasseWithAttendance>> callback) {
        enqueueList(api.listJoinedClassesWithStats(), ClasseWithAttendanceDto::toDomain, callback,
                "Impossible de charger les classes");
    }

    // --- helpers ---

    // Listes : transforme et délivre, ou erreur HTTP/réseau.
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

    // Singletons : transforme et délivre, ou erreur HTTP/réseau.
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
