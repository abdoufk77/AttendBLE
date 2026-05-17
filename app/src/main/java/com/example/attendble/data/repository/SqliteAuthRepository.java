package com.example.attendble.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.attendble.data.local.AsyncRunner;
import com.example.attendble.data.local.AttendBleDbHelper;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.model.User;
import com.example.attendble.domain.repository.AuthRepository;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.UUID;

/** Implémentation SQLite de {@link AuthRepository}. */
public class SqliteAuthRepository implements AuthRepository {

    private final AttendBleDbHelper helper;
    private String currentUserId;

    public SqliteAuthRepository(AttendBleDbHelper helper) {
        this.helper = helper;
    }

    @Override
    public void login(String email, String password, Callback<User> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor c = db.query(AttendBleDbHelper.T_USERS, null,
                    AttendBleDbHelper.C_U_EMAIL + " = ?", new String[]{email},
                    null, null, null)) {
                if (!c.moveToFirst()) throw new Exception("Email introuvable");
                String storedPwd = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_PASSWORD));
                if (!storedPwd.equals(password)) throw new Exception("Mot de passe incorrect");
                User user = readUser(c);
                currentUserId = user.getUid();
                return user;
            }
        }, callback);
    }

    @Override
    public void signupProfesseur(String email, String password, Professeur professeur, Callback<Professeur> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            if (emailExists(db, email)) throw new Exception("Email déjà utilisé");
            String uid = UUID.randomUUID().toString();
            professeur.setUid(uid);
            professeur.setEmail(email);

            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_U_UID, uid);
            v.put(AttendBleDbHelper.C_U_EMAIL, email);
            v.put(AttendBleDbHelper.C_U_NOM, professeur.getNom());
            v.put(AttendBleDbHelper.C_U_PHOTO, professeur.getPhotoUrl());
            v.put(AttendBleDbHelper.C_U_ROLE, UserRole.PROFESSEUR.name());
            v.put(AttendBleDbHelper.C_U_PASSWORD, password);
            v.put(AttendBleDbHelper.C_U_DEPARTMENT, professeur.getDepartment());
            v.put(AttendBleDbHelper.C_U_DATE_INSCRIPTION, System.currentTimeMillis());
            db.insertOrThrow(AttendBleDbHelper.T_USERS, null, v);
            currentUserId = uid;
            return professeur;
        }, callback);
    }

    @Override
    public void signupEtudiant(String email, String password, Etudiant etudiant, Callback<Etudiant> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            if (emailExists(db, email)) throw new Exception("Email déjà utilisé");
            String uid = UUID.randomUUID().toString();
            etudiant.setUid(uid);
            etudiant.setEmail(email);

            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_U_UID, uid);
            v.put(AttendBleDbHelper.C_U_EMAIL, email);
            v.put(AttendBleDbHelper.C_U_NOM, etudiant.getNom());
            v.put(AttendBleDbHelper.C_U_PHOTO, etudiant.getPhotoUrl());
            v.put(AttendBleDbHelper.C_U_ROLE, UserRole.ETUDIANT.name());
            v.put(AttendBleDbHelper.C_U_PASSWORD, password);
            v.put(AttendBleDbHelper.C_U_NUM_ETUD, etudiant.getNumEtud());
            if (etudiant.getFaceEmbedding() != null) {
                v.put(AttendBleDbHelper.C_U_FACE_EMBEDDING, floatsToBytes(etudiant.getFaceEmbedding()));
            }
            v.put(AttendBleDbHelper.C_U_DATE_INSCRIPTION, System.currentTimeMillis());
            db.insertOrThrow(AttendBleDbHelper.T_USERS, null, v);
            currentUserId = uid;
            return etudiant;
        }, callback);
    }

    @Override
    public void logout() {
        currentUserId = null;
    }

    @Override
    public String getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    @Override
    public void getCurrentUser(Callback<User> callback) {
        AsyncRunner.run(() -> {
            if (currentUserId == null) throw new Exception("Pas d'utilisateur connecté");
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor c = db.query(AttendBleDbHelper.T_USERS, null,
                    AttendBleDbHelper.C_U_UID + " = ?", new String[]{currentUserId},
                    null, null, null)) {
                if (!c.moveToFirst()) throw new Exception("Utilisateur introuvable : " + currentUserId);
                return readUser(c);
            }
        }, callback);
    }

    @Override
    public void updateFaceEmbedding(String uid, float[] embedding, Callback<Void> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_U_FACE_EMBEDDING, floatsToBytes(embedding));
            int updated = db.update(AttendBleDbHelper.T_USERS, v,
                    AttendBleDbHelper.C_U_UID + " = ? AND " + AttendBleDbHelper.C_U_ROLE + " = ?",
                    new String[]{uid, UserRole.ETUDIANT.name()});
            if (updated == 0) throw new Exception("Étudiant introuvable : " + uid);
            Log.i("SqliteAuthRepo", "updateFaceEmbedding uid=" + uid
                    + " bytes=" + (embedding.length * 4) + " rowsUpdated=" + updated);
            return null;
        }, callback);
    }

    private boolean emailExists(SQLiteDatabase db, String email) {
        try (Cursor c = db.query(AttendBleDbHelper.T_USERS, new String[]{AttendBleDbHelper.C_U_UID},
                AttendBleDbHelper.C_U_EMAIL + " = ?", new String[]{email},
                null, null, null)) {
            return c.moveToFirst();
        }
    }

    private User readUser(Cursor c) {
        String uid = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_UID));
        String email = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_EMAIL));
        String nom = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_NOM));
        String photo = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_PHOTO));
        String roleStr = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_ROLE));
        UserRole role = UserRole.fromString(roleStr);
        if (role == UserRole.PROFESSEUR) {
            String dept = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_DEPARTMENT));
            return new Professeur(uid, email, nom, photo, dept);
        } else {
            String num = c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_NUM_ETUD));
            Etudiant e = new Etudiant(uid, email, nom, photo, num);
            int embIdx = c.getColumnIndexOrThrow(AttendBleDbHelper.C_U_FACE_EMBEDDING);
            if (!c.isNull(embIdx)) {
                e.setFaceEmbedding(bytesToFloats(c.getBlob(embIdx)));
            }
            return e;
        }
    }

    private static byte[] floatsToBytes(float[] floats) {
        ByteBuffer buf = ByteBuffer.allocate(floats.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        buf.asFloatBuffer().put(floats);
        return buf.array();
    }

    private static float[] bytesToFloats(byte[] bytes) {
        FloatBuffer fb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        float[] out = new float[fb.remaining()];
        fb.get(out);
        return out;
    }
}
