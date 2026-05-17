package com.example.attendble.data.remote;

import com.example.attendble.data.remote.dto.AuthResponseDto;
import com.example.attendble.data.remote.dto.ClasseDto;
import com.example.attendble.data.remote.dto.ClasseWithAttendanceDto;
import com.example.attendble.data.remote.dto.CreateClasseRequestDto;
import com.example.attendble.data.remote.dto.EtudiantAttendanceDto;
import com.example.attendble.data.remote.dto.JoinClasseRequestDto;
import com.example.attendble.data.remote.dto.LoginRequestDto;
import com.example.attendble.data.remote.dto.MembreDto;
import com.example.attendble.data.remote.dto.ProfStatsDto;
import com.example.attendble.data.remote.dto.SignupRequestDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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
}
