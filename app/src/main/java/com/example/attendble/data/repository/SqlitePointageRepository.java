package com.example.attendble.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.attendble.data.local.AsyncRunner;
import com.example.attendble.data.local.AttendBleDbHelper;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.PointageStatut;
import com.example.attendble.domain.model.Pointage;
import com.example.attendble.domain.repository.PointageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Implémentation SQLite de {@link PointageRepository}. */
public class SqlitePointageRepository implements PointageRepository {

    private final AttendBleDbHelper helper;

    public SqlitePointageRepository(AttendBleDbHelper helper) {
        this.helper = helper;
    }

    @Override
    public void validerPresence(Pointage pointage, String codeSaisi, Callback<Pointage> callback) {
        // codeSaisi ignoré : la validation locale a déjà eu lieu dans ValiderPresenceUseCase.
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            if (pointage.getPointageId() == null) pointage.setPointageId(UUID.randomUUID().toString());
            if (pointage.getStatut() == null) pointage.setStatut(PointageStatut.PRESENT);
            if (pointage.getHeurePointage() == 0) pointage.setHeurePointage(System.currentTimeMillis());

            if (existsForSession(db, pointage.getSessionId(), pointage.getEtudiantId())) {
                throw new Exception("Vous avez déjà pointé pour cette session");
            }

            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_P_POINTAGE_ID, pointage.getPointageId());
            v.put(AttendBleDbHelper.C_P_SESSION_ID, pointage.getSessionId());
            v.put(AttendBleDbHelper.C_P_ETUDIANT_ID, pointage.getEtudiantId());
            v.put(AttendBleDbHelper.C_P_HEURE, pointage.getHeurePointage());
            v.put(AttendBleDbHelper.C_P_STATUT, pointage.getStatut().name());
            v.put(AttendBleDbHelper.C_P_BLE_DETECTE, pointage.isBleDetecte() ? 1 : 0);
            v.put(AttendBleDbHelper.C_P_FACE_VERIFIED, pointage.isFaceVerified() ? 1 : 0);
            db.insertOrThrow(AttendBleDbHelper.T_POINTAGES, null, v);
            return pointage;
        }, callback);
    }

    @Override
    public void listBySession(String sessionId, Callback<List<Pointage>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Pointage> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_POINTAGES, null,
                    AttendBleDbHelper.C_P_SESSION_ID + " = ?", new String[]{sessionId},
                    null, null, AttendBleDbHelper.C_P_HEURE + " ASC")) {
                while (c.moveToNext()) result.add(readPointage(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void listByEtudiant(String etudiantId, Callback<List<Pointage>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Pointage> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_POINTAGES, null,
                    AttendBleDbHelper.C_P_ETUDIANT_ID + " = ?", new String[]{etudiantId},
                    null, null, AttendBleDbHelper.C_P_HEURE + " DESC")) {
                while (c.moveToNext()) result.add(readPointage(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void hasPointage(String sessionId, String etudiantId, Callback<Boolean> callback) {
        AsyncRunner.run(() -> existsForSession(helper.getReadableDatabase(), sessionId, etudiantId),
                callback);
    }

    private boolean existsForSession(SQLiteDatabase db, String sessionId, String etudiantId) {
        try (Cursor c = db.query(AttendBleDbHelper.T_POINTAGES,
                new String[]{AttendBleDbHelper.C_P_POINTAGE_ID},
                AttendBleDbHelper.C_P_SESSION_ID + " = ? AND " + AttendBleDbHelper.C_P_ETUDIANT_ID + " = ?",
                new String[]{sessionId, etudiantId}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    private Pointage readPointage(Cursor c) {
        Pointage p = new Pointage();
        p.setPointageId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_POINTAGE_ID)));
        p.setSessionId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_SESSION_ID)));
        p.setEtudiantId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_ETUDIANT_ID)));
        p.setHeurePointage(c.getLong(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_HEURE)));
        p.setStatut(PointageStatut.fromString(
                c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_STATUT))));
        p.setBleDetecte(c.getInt(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_BLE_DETECTE)) == 1);
        p.setFaceVerified(c.getInt(c.getColumnIndexOrThrow(AttendBleDbHelper.C_P_FACE_VERIFIED)) == 1);
        return p;
    }
}
