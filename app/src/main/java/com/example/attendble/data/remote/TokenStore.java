package com.example.attendble.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

// Stockage simple du JWT + uid + role en SharedPreferences.
// Pour la prod, migrer vers EncryptedSharedPreferences (lib androidx.security).
public class TokenStore {

    private static final String PREFS = "attendble_auth";
    private static final String K_TOKEN = "token";
    private static final String K_UID = "uid";
    private static final String K_ROLE = "role";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(String token, String uid, String role) {
        prefs.edit().putString(K_TOKEN, token).putString(K_UID, uid).putString(K_ROLE, role).apply();
    }

    public String getToken() {
        return prefs.getString(K_TOKEN, null);
    }

    public String getUid() {
        return prefs.getString(K_UID, null);
    }

    public String getRole() {
        return prefs.getString(K_ROLE, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
