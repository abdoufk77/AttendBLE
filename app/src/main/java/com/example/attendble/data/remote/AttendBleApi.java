package com.example.attendble.data.remote;

import com.example.attendble.data.remote.dto.AuthResponseDto;
import com.example.attendble.data.remote.dto.ClasseDto;
import com.example.attendble.data.remote.dto.ClasseWithAttendanceDto;
import com.example.attendble.data.remote.dto.CreateClasseRequestDto;
import com.example.attendble.data.remote.dto.EtudiantAttendanceDto;
import com.example.attendble.data.remote.dto.JoinClasseRequestDto;
import com.example.attendble.data.remote.dto.LoginRequestDto;
import com.example.attendble.data.remote.dto.MarquerPresenceRequestDto;
import com.example.attendble.data.remote.dto.MembreDto;
import com.example.attendble.data.remote.dto.OpenSessionRequestDto;
import com.example.attendble.data.remote.dto.PointageDto;
import com.example.attendble.data.remote.dto.ProfStatsDto;
import com.example.attendble.data.remote.dto.RefreshCodeRequestDto;
import com.example.attendble.data.remote.dto.SessionDto;
import com.example.attendble.data.remote.dto.SignupRequestDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Interface Retrofit unique vers le backend Spring Boot.
public interface AttendBleApi {

    // --- Auth ---
    @POST("api/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto body);

    @POST("api/auth/signup")
    Call<AuthResponseDto> signup(@Body SignupRequestDto body);

    // --- Classes ---
    @POST("api/classes")
    Call<ClasseDto> createClasse(@Body CreateClasseRequestDto body);

    @GET("api/classes/mine")
    Call<List<ClasseDto>> listMyClasses();

    @GET("api/classes/joined")
    Call<List<ClasseDto>> listJoinedClasses();

    @GET("api/classes/today")
    Call<List<ClasseDto>> listTodayMyClasses(@Query("jourSemaine") int jourSemaine);

    @GET("api/classes/joined/today")
    Call<List<ClasseDto>> listTodayJoinedClasses(@Query("jourSemaine") int jourSemaine);

    @GET("api/classes/{classeId}")
    Call<ClasseDto> findClasseById(@Path("classeId") String classeId);

    @GET("api/classes/by-code/{code}")
    Call<ClasseDto> findClasseByCode(@Path("code") String code);

    @POST("api/classes/join")
    Call<ClasseDto> joinClasse(@Body JoinClasseRequestDto body);

    @GET("api/classes/{classeId}/membres")
    Call<List<MembreDto>> listMembres(@Path("classeId") String classeId);

    @GET("api/classes/{classeId}/etudiants-attendance")
    Call<List<EtudiantAttendanceDto>> listEtudiantsAttendance(@Path("classeId") String classeId);

    @GET("api/classes/stats/prof")
    Call<ProfStatsDto> getProfStats();

    @GET("api/classes/with-stats")
    Call<List<ClasseWithAttendanceDto>> listMyClassesWithStats();

    @GET("api/classes/joined/with-stats")
    Call<List<ClasseWithAttendanceDto>> listJoinedClassesWithStats();

    // --- Sessions ---
    @POST("api/sessions")
    Call<SessionDto> openSession(@Body OpenSessionRequestDto body);

    @PATCH("api/sessions/{sessionId}/refresh-code")
    Call<SessionDto> refreshSessionCode(@Path("sessionId") String sessionId,
                                        @Body RefreshCodeRequestDto body);

    @POST("api/sessions/{sessionId}/close")
    Call<SessionDto> closeSession(@Path("sessionId") String sessionId);

    @GET("api/sessions/{sessionId}")
    Call<SessionDto> getSession(@Path("sessionId") String sessionId);

    @GET("api/sessions/active")
    Call<SessionDto> getActiveSession(@Query("classeId") String classeId);

    @GET("api/sessions")
    Call<List<SessionDto>> listSessionsByClasse(@Query("classeId") String classeId);

    @GET("api/sessions/active-all")
    Call<List<SessionDto>> listAllActiveSessions();

    // --- Pointages ---
    @POST("api/pointages")
    Call<PointageDto> marquerPresence(@Body MarquerPresenceRequestDto body);

    @GET("api/pointages")
    Call<List<PointageDto>> listPointagesBySession(@Query("sessionId") String sessionId);

    @GET("api/pointages/mine")
    Call<List<PointageDto>> listMyPointages();

    @GET("api/pointages/check")
    Call<Map<String, Boolean>> checkHasPointage(@Query("sessionId") String sessionId);
}
