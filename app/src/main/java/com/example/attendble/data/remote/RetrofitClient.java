package com.example.attendble.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Construit une instance unique de Retrofit pointant vers le backend Spring Boot.
//
// BASE_URL :
//   - Émulateur Android Studio  → http://10.0.2.2:8080/  (10.0.2.2 = localhost du PC hôte)
//   - Vrai téléphone sur LAN    → http://<IP-PC>:8080/   (à mettre à jour selon ton réseau)
//   - Prod Azure                → https://api.attendble.<domaine>/
public final class RetrofitClient {

    public static final String BASE_URL = "http://192.168.0.103:8080/";

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
