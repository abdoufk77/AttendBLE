package com.example.attendble.data.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// Injecte "Authorization: Bearer <jwt>" sur chaque requête sortante quand un token est stocké.
// Skip les endpoints publics (/api/auth/**).
public class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();
        if (path.startsWith("/api/auth/")) {
            return chain.proceed(original);
        }
        String token = tokenStore.getToken();
        if (token == null) {
            return chain.proceed(original);
        }
        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authed);
    }
}
