package com.example.attendble.data.remote;

import com.example.attendble.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Construit une instance unique de Retrofit pointant vers le backend Spring Boot.
// BASE_URL est injectée depuis local.properties (clés attendble.backend.host / port)
// via build.gradle.kts → BuildConfig.BACKEND_BASE_URL. Pour changer de Wi-Fi/hotspot :
//   1. ipconfig pour récupérer la nouvelle IP
//   2. édite local.properties (clé attendble.backend.host)
//   3. rebuild — aucune autre modif de code/XML nécessaire.
public final class RetrofitClient {

    public static final String BASE_URL = BuildConfig.BACKEND_BASE_URL;

    private static volatile AttendBleApi api;

    private RetrofitClient() {
    }

    public static AttendBleApi getApi(TokenStore tokenStore) {
        if (api == null) {
            synchronized (RetrofitClient.class) {
                if (api == null) {
                    api = build(tokenStore);
                }
            }
        }
        return api;
    }

    private static AttendBleApi build(TokenStore tokenStore) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(AttendBleApi.class);
    }
}
