package com.example.attendble.data.remote;

import com.example.attendble.data.remote.dto.AuthResponseDto;
import com.example.attendble.data.remote.dto.LoginRequestDto;
import com.example.attendble.data.remote.dto.SignupRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Interface Retrofit unique vers le backend Spring Boot.
// Les autres modules (classes, sessions, pointages) viendront s'ajouter ici.
public interface AttendBleApi {

    @POST("api/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto body);

    @POST("api/auth/signup")
    Call<AuthResponseDto> signup(@Body SignupRequestDto body);
}
