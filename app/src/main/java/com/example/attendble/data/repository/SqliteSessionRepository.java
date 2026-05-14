package com.example.attendble.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.attendble.data.local.AsyncRunner;
import com.example.attendble.data.local.AttendBleDbHelper;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.SessionStatus;
import com.example.attendble.domain.model.Session;
import com.example.attendble.domain.repository.SessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Implémentation SQLite de {@link SessionRepository}. */
public class SqliteSessionRepository implements SessionRepository {

    private final AttendBleDbHelper helper;

    public SqliteSessionRepository(AttendBleDbHelper helper) {
        this.helper = helper;
    }

    @Override
    public void ouvrirSession(Session session, Callback<Session> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            if (session.getSessionId() == null) session.setSessionId(UUID.randomUUID().toString());
            if (session.getStatut() == null) session.setStatut(SessionStatus.ACTIVE);
            if (session.getDateOuverture() == 0) session.setDateOuverture(System.currentTimeMillis());

            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_S_SESSION_ID, session.getSessionId());
            v.put(AttendBleDbHelper.C_S_CLASSE_ID, session.getClasseId());
            v.put(AttendBleDbHelper.C_S_CODE_TEMP, session.getCodeTemp());
            v.put(AttendBleDbHelper.C_S_CODE_EXPIRE_AT, session.getCodeExpireAt());
            v.put(AttendBleDbHelper.C_S_BEACON_UUID, session.getBeaconUUID());
            v.put(AttendBleDbHelper.C_S_STATUT, session.getStatut().name());
            v.put(AttendBleDbHelper.C_S_DATE_OUVERTURE, session.getDateOuverture());
            v.put(AttendBleDbHelper.C_S_DATE_FERMETURE, session.getDateFermeture());
            db.insertOrThrow(AttendBleDbHelper.T_SESSIONS, null, v);
            return session;
        }, callback);
    }

    @Override
    public void refreshCode(String sessionId, String nouveauCode, long expireAt, Callback<Session> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_S_CODE_TEMP, nouveauCode);
            v.put(AttendBleDbHelper.C_S_CODE_EXPIRE_AT, expireAt);
            int updated = db.update(AttendBleDbHelper.T_SESSIONS, v,
                    AttendBleDbHelper.C_S_SESSION_ID + " = ?", new String[]{sessionId});
            if (updated == 0) throw new Exception("Session introuvable : " + sessionId);
            return findByIdSync(db, sessionId);
        }, callback);
    }

    @Override
    public void fermerSession(String sessionId, Callback<Session> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_S_STATUT, SessionStatus.FERMEE.name());
            v.put(AttendBleDbHelper.C_S_DATE_FERMETURE, System.currentTimeMillis());
            int updated = db.update(AttendBleDbHelper.T_SESSIONS, v,
                    AttendBleDbHelper.C_S_SESSION_ID + " = ?", new String[]{sessionId});
            if (updated == 0) throw new Exception("Session introuvable : " + sessionId);
            return findByIdSync(db, sessionId);
        }, callback);
    }

    @Override
    public void getActiveByClasse(String classeId, Callback<Session> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor c = db.query(AttendBleDbHelper.T_SESSIONS, null,
                    AttendBleDbHelper.C_S_CLASSE_ID + " = ? AND " + AttendBleDbHelper.C_S_STATUT + " = ?",
                    new String[]{classeId, SessionStatus.ACTIVE.name()},
                    null, null, AttendBleDbHelper.C_S_DATE_OUVERTURE + " DESC", "1")) {
                if (!c.moveToFirst()) throw new Exception("Aucune session active pour cette classe");
                return readSession(c);
            }
        }, callback);
    }

    @Override
    public void findById(String sessionId, Callback<Session> callback) {
        AsyncRunner.run(() -> findByIdSync(helper.getReadableDatabase(), sessionId), callback);
    }

    @Override
    public void listByClasse(String classeId, Callback<List<Session>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Session> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_SESSIONS, null,
                    AttendBleDbHelper.C_S_CLASSE_ID + " = ?", new String[]{classeId},
                    null, null, AttendBleDbHelper.C_S_DATE_OUVERTURE + " DESC")) {
                while (c.moveToNext()) result.add(readSession(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void listAllActive(Callback<List<Session>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Session> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_SESSIONS, null,
                    AttendBleDbHelper.C_S_STATUT + " = ?",
                    new String[]{SessionStatus.ACTIVE.name()},
                    null, null, AttendBleDbHelper.C_S_DATE_OUVERTURE + " DESC")) {
                while (c.moveToNext()) result.add(readSession(c));
            }
            return result;
        }, callback);
    }

    private Session findByIdSync(SQLiteDatabase db, String sessionId) throws Exception {
        try (Cursor c = db.query(AttendBleDbHelper.T_SESSIONS, null,
                AttendBleDbHelper.C_S_SESSION_ID + " = ?", new String[]{sessionId},
                null, null, null)) {
            if (!c.moveToFirst()) throw new Exception("Session introuvable : " + sessionId);
            return readSession(c);
        }
    }

    private Session readSession(Cursor c) {
        Session s = new Session();
        s.setSessionId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_SESSION_ID)));
        s.setClasseId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_CLASSE_ID)));
        s.setCodeTemp(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_CODE_TEMP)));
        s.setCodeExpireAt(c.getLong(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_CODE_EXPIRE_AT)));
        s.setBeaconUUID(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_BEACON_UUID)));
        s.setStatut(SessionStatus.fromString(
                c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_STATUT))));
        s.setDateOuverture(c.getLong(c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_DATE_OUVERTURE)));
        int fermIdx = c.getColumnIndexOrThrow(AttendBleDbHelper.C_S_DATE_FERMETURE);
        s.setDateFermeture(c.isNull(fermIdx) ? null : c.getLong(fermIdx));
        return s;
    }
}
