package com.example.attendble.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.attendble.data.local.AsyncRunner;
import com.example.attendble.data.local.AttendBleDbHelper;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.enums.PointageStatut;
import com.example.attendble.domain.enums.SessionStatus;
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.EtudiantAttendance;
import com.example.attendble.domain.model.ProfStats;
import com.example.attendble.domain.repository.ClasseRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Implémentation SQLite de {@link ClasseRepository}. */
public class SqliteClasseRepository implements ClasseRepository {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_SUFFIX_LENGTH = 3;
    private static final int MAX_GENERATION_RETRIES = 10;

    private final AttendBleDbHelper helper;
    private final SecureRandom random = new SecureRandom();

    public SqliteClasseRepository(AttendBleDbHelper helper) {
        this.helper = helper;
    }

    @Override
    public void creerClasse(Classe classe, Callback<Classe> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            String code = generateUniqueCode(db, classe.getGroupe(), classe.getNom());
            if (code == null) throw new IllegalStateException("Impossible de générer un code unique");
            classe.setClasseId(UUID.randomUUID().toString());
            classe.setCodeInvitation(code);

            ContentValues v = new ContentValues();
            v.put(AttendBleDbHelper.C_C_CLASSE_ID, classe.getClasseId());
            v.put(AttendBleDbHelper.C_C_NOM, classe.getNom());
            v.put(AttendBleDbHelper.C_C_MATIERE, classe.getMatiere());
            v.put(AttendBleDbHelper.C_C_GROUPE, classe.getGroupe());
            v.put(AttendBleDbHelper.C_C_SALLE, classe.getSalle());
            v.put(AttendBleDbHelper.C_C_HORAIRE, classe.getHoraire());
            v.put(AttendBleDbHelper.C_C_JOUR_SEMAINE, classe.getJourSemaine());
            v.put(AttendBleDbHelper.C_C_HEURE_DEBUT, classe.getHeureDebut());
            v.put(AttendBleDbHelper.C_C_HEURE_FIN, classe.getHeureFin());
            v.put(AttendBleDbHelper.C_C_NB_ETUDIANTS, classe.getNbEtudiants());
            v.put(AttendBleDbHelper.C_C_CODE_INVITATION, code);
            v.put(AttendBleDbHelper.C_C_PROFESSEUR_ID, classe.getProfesseurId());
            v.put(AttendBleDbHelper.C_C_DATE_CREATION, System.currentTimeMillis());
            db.insertOrThrow(AttendBleDbHelper.T_CLASSES, null, v);
            return classe;
        }, callback);
    }

    @Override
    public void listClassesByProfesseur(String professeurId, Callback<List<Classe>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Classe> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_CLASSES, null,
                    AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ?", new String[]{professeurId},
                    null, null, AttendBleDbHelper.C_C_DATE_CREATION + " DESC")) {
                while (c.moveToNext()) result.add(readClasse(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void findById(String classeId, Callback<Classe> callback) {
        AsyncRunner.run(() -> {
            if (classeId == null) throw new Exception("Classe introuvable");
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor c = db.query(AttendBleDbHelper.T_CLASSES, null,
                    AttendBleDbHelper.C_C_CLASSE_ID + " = ?", new String[]{classeId},
                    null, null, null)) {
                if (!c.moveToFirst()) throw new Exception("Classe introuvable");
                return readClasse(c);
            }
        }, callback);
    }

    @Override
    public void findByCodeInvitation(String codeInvitation, Callback<Classe> callback) {
        AsyncRunner.run(() -> {
            Classe classe = lookupByCode(helper.getReadableDatabase(), codeInvitation);
            if (classe == null) throw new Exception("Code d'invitation invalide");
            return classe;
        }, callback);
    }

    @Override
    public void joinClasseByCode(String codeInvitation, String etudiantId, Callback<Classe> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            Classe classe = lookupByCode(db, codeInvitation);
            if (classe == null) throw new Exception("Code d'invitation invalide");

            try (Cursor c = db.query(AttendBleDbHelper.T_ENROLLMENTS,
                    new String[]{AttendBleDbHelper.C_E_CLASSE_ID},
                    AttendBleDbHelper.C_E_CLASSE_ID + " = ? AND " + AttendBleDbHelper.C_E_ETUDIANT_ID + " = ?",
                    new String[]{classe.getClasseId(), etudiantId}, null, null, null)) {
                if (c.moveToFirst()) throw new Exception("Vous êtes déjà inscrit à cette classe");
            }

            db.beginTransaction();
            try {
                ContentValues v = new ContentValues();
                v.put(AttendBleDbHelper.C_E_CLASSE_ID, classe.getClasseId());
                v.put(AttendBleDbHelper.C_E_ETUDIANT_ID, etudiantId);
                v.put(AttendBleDbHelper.C_E_DATE, System.currentTimeMillis());
                db.insertOrThrow(AttendBleDbHelper.T_ENROLLMENTS, null, v);

                db.execSQL("UPDATE " + AttendBleDbHelper.T_CLASSES
                        + " SET " + AttendBleDbHelper.C_C_NB_ETUDIANTS + " = "
                        + AttendBleDbHelper.C_C_NB_ETUDIANTS + " + 1"
                        + " WHERE " + AttendBleDbHelper.C_C_CLASSE_ID + " = ?",
                        new Object[]{classe.getClasseId()});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            classe.setNbEtudiants(classe.getNbEtudiants() + 1);
            return classe;
        }, callback);
    }

    @Override
    public void listClassesByEtudiant(String etudiantId, Callback<List<Classe>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Classe> result = new ArrayList<>();
            String sql = "SELECT c.* FROM " + AttendBleDbHelper.T_CLASSES + " c"
                    + " INNER JOIN " + AttendBleDbHelper.T_ENROLLMENTS + " e"
                    + " ON c." + AttendBleDbHelper.C_C_CLASSE_ID + " = e." + AttendBleDbHelper.C_E_CLASSE_ID
                    + " WHERE e." + AttendBleDbHelper.C_E_ETUDIANT_ID + " = ?"
                    + " ORDER BY e." + AttendBleDbHelper.C_E_DATE + " DESC";
            try (Cursor c = db.rawQuery(sql, new String[]{etudiantId})) {
                while (c.moveToNext()) result.add(readClasse(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void listTodayByProfesseur(String professeurId, int jourSemaine, Callback<List<Classe>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Classe> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_CLASSES, null,
                    AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ? AND "
                            + AttendBleDbHelper.C_C_JOUR_SEMAINE + " = ?",
                    new String[]{professeurId, String.valueOf(jourSemaine)},
                    null, null, AttendBleDbHelper.C_C_HEURE_DEBUT + " ASC")) {
                while (c.moveToNext()) result.add(readClasse(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void listTodayByEtudiant(String etudiantId, int jourSemaine, Callback<List<Classe>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<Classe> result = new ArrayList<>();
            String sql = "SELECT c.* FROM " + AttendBleDbHelper.T_CLASSES + " c"
                    + " INNER JOIN " + AttendBleDbHelper.T_ENROLLMENTS + " e"
                    + " ON c." + AttendBleDbHelper.C_C_CLASSE_ID + " = e." + AttendBleDbHelper.C_E_CLASSE_ID
                    + " WHERE e." + AttendBleDbHelper.C_E_ETUDIANT_ID + " = ?"
                    + " AND c." + AttendBleDbHelper.C_C_JOUR_SEMAINE + " = ?"
                    + " ORDER BY c." + AttendBleDbHelper.C_C_HEURE_DEBUT + " ASC";
            try (Cursor c = db.rawQuery(sql, new String[]{etudiantId, String.valueOf(jourSemaine)})) {
                while (c.moveToNext()) result.add(readClasse(c));
            }
            return result;
        }, callback);
    }

    @Override
    public void listEtudiantsByClasse(String classeId, Callback<List<EtudiantAttendance>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<EtudiantAttendance> result = new ArrayList<>();

            int totalSessionsFermees;
            try (Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_SESSIONS
                            + " WHERE " + AttendBleDbHelper.C_S_CLASSE_ID + " = ?"
                            + " AND " + AttendBleDbHelper.C_S_STATUT + " = ?",
                    new String[]{classeId, SessionStatus.FERMEE.name()})) {
                totalSessionsFermees = c.moveToFirst() ? c.getInt(0) : 0;
            }

            String sql = "SELECT u." + AttendBleDbHelper.C_U_UID
                    + ", u." + AttendBleDbHelper.C_U_EMAIL
                    + ", u." + AttendBleDbHelper.C_U_NOM
                    + ", u." + AttendBleDbHelper.C_U_PHOTO
                    + ", u." + AttendBleDbHelper.C_U_NUM_ETUD
                    + ", ("
                    + "  SELECT COUNT(*) FROM " + AttendBleDbHelper.T_POINTAGES + " p"
                    + "  INNER JOIN " + AttendBleDbHelper.T_SESSIONS + " s"
                    + "    ON p." + AttendBleDbHelper.C_P_SESSION_ID + " = s." + AttendBleDbHelper.C_S_SESSION_ID
                    + "  WHERE s." + AttendBleDbHelper.C_S_CLASSE_ID + " = ?"
                    + "    AND p." + AttendBleDbHelper.C_P_ETUDIANT_ID + " = u." + AttendBleDbHelper.C_U_UID
                    + "    AND p." + AttendBleDbHelper.C_P_STATUT + " = ?"
                    + ") AS nb_presences"
                    + " FROM " + AttendBleDbHelper.T_USERS + " u"
                    + " INNER JOIN " + AttendBleDbHelper.T_ENROLLMENTS + " e"
                    + "   ON u." + AttendBleDbHelper.C_U_UID + " = e." + AttendBleDbHelper.C_E_ETUDIANT_ID
                    + " WHERE e." + AttendBleDbHelper.C_E_CLASSE_ID + " = ?"
                    + "   AND u." + AttendBleDbHelper.C_U_ROLE + " = ?"
                    + " ORDER BY u." + AttendBleDbHelper.C_U_NOM + " ASC";
            try (Cursor c = db.rawQuery(sql, new String[]{
                    classeId, PointageStatut.PRESENT.name(), classeId, UserRole.ETUDIANT.name()})) {
                while (c.moveToNext()) {
                    Etudiant etu = new Etudiant(
                            c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                    int nbPresences = c.getInt(5);
                    result.add(new EtudiantAttendance(etu, nbPresences, totalSessionsFermees));
                }
            }
            return result;
        }, callback);
    }

    @Override
    public void getProfStats(String professeurId, Callback<ProfStats> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();

            int totalClasses;
            try (Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_CLASSES
                            + " WHERE " + AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ?",
                    new String[]{professeurId})) {
                totalClasses = c.moveToFirst() ? c.getInt(0) : 0;
            }

            int totalStudents;
            try (Cursor c = db.rawQuery(
                    "SELECT COUNT(DISTINCT e." + AttendBleDbHelper.C_E_ETUDIANT_ID + ")"
                            + " FROM " + AttendBleDbHelper.T_ENROLLMENTS + " e"
                            + " INNER JOIN " + AttendBleDbHelper.T_CLASSES + " c"
                            + "   ON e." + AttendBleDbHelper.C_E_CLASSE_ID + " = c." + AttendBleDbHelper.C_C_CLASSE_ID
                            + " WHERE c." + AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ?",
                    new String[]{professeurId})) {
                totalStudents = c.moveToFirst() ? c.getInt(0) : 0;
            }

            // Pointages attendus = (étudiants inscrits par classe) × (sessions FERMEE de la classe)
            int totalExpected;
            try (Cursor c = db.rawQuery(
                    "SELECT COALESCE(SUM(nb_inscrits * nb_sessions), 0) FROM ("
                            + "  SELECT cls." + AttendBleDbHelper.C_C_CLASSE_ID + ","
                            + "    (SELECT COUNT(*) FROM " + AttendBleDbHelper.T_ENROLLMENTS + " e"
                            + "       WHERE e." + AttendBleDbHelper.C_E_CLASSE_ID + " = cls." + AttendBleDbHelper.C_C_CLASSE_ID + ") AS nb_inscrits,"
                            + "    (SELECT COUNT(*) FROM " + AttendBleDbHelper.T_SESSIONS + " s"
                            + "       WHERE s." + AttendBleDbHelper.C_S_CLASSE_ID + " = cls." + AttendBleDbHelper.C_C_CLASSE_ID
                            + "         AND s." + AttendBleDbHelper.C_S_STATUT + " = '" + SessionStatus.FERMEE.name() + "') AS nb_sessions"
                            + "  FROM " + AttendBleDbHelper.T_CLASSES + " cls"
                            + "  WHERE cls." + AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ?"
                            + ")",
                    new String[]{professeurId})) {
                totalExpected = c.moveToFirst() ? c.getInt(0) : 0;
            }

            int totalPresent;
            try (Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_POINTAGES + " p"
                            + " INNER JOIN " + AttendBleDbHelper.T_SESSIONS + " s"
                            + "   ON p." + AttendBleDbHelper.C_P_SESSION_ID + " = s." + AttendBleDbHelper.C_S_SESSION_ID
                            + " INNER JOIN " + AttendBleDbHelper.T_CLASSES + " cls"
                            + "   ON s." + AttendBleDbHelper.C_S_CLASSE_ID + " = cls." + AttendBleDbHelper.C_C_CLASSE_ID
                            + " WHERE cls." + AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ?"
                            + "   AND p." + AttendBleDbHelper.C_P_STATUT + " = ?"
                            + "   AND s." + AttendBleDbHelper.C_S_STATUT + " = ?",
                    new String[]{professeurId, PointageStatut.PRESENT.name(), SessionStatus.FERMEE.name()})) {
                totalPresent = c.moveToFirst() ? c.getInt(0) : 0;
            }

            int avg = totalExpected == 0 ? 100 : Math.round(100f * totalPresent / totalExpected);
            return new ProfStats(totalStudents, avg, totalClasses);
        }, callback);
    }

    @Override
    public void listClassesByProfesseurWithStats(String professeurId, Callback<List<ClasseWithAttendance>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<ClasseWithAttendance> result = new ArrayList<>();
            try (Cursor c = db.query(AttendBleDbHelper.T_CLASSES, null,
                    AttendBleDbHelper.C_C_PROFESSEUR_ID + " = ?", new String[]{professeurId},
                    null, null, AttendBleDbHelper.C_C_DATE_CREATION + " DESC")) {
                while (c.moveToNext()) {
                    Classe k = readClasse(c);
                    int[] stats = computeAvgAttendance(db, k.getClasseId());
                    result.add(new ClasseWithAttendance(k, stats[0], stats[1]));
                }
            }
            return result;
        }, callback);
    }

    @Override
    public void listClassesByEtudiantWithStats(String etudiantId, Callback<List<ClasseWithAttendance>> callback) {
        AsyncRunner.run(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            List<ClasseWithAttendance> result = new ArrayList<>();
            String sql = "SELECT c.* FROM " + AttendBleDbHelper.T_CLASSES + " c"
                    + " INNER JOIN " + AttendBleDbHelper.T_ENROLLMENTS + " e"
                    + " ON c." + AttendBleDbHelper.C_C_CLASSE_ID + " = e." + AttendBleDbHelper.C_E_CLASSE_ID
                    + " WHERE e." + AttendBleDbHelper.C_E_ETUDIANT_ID + " = ?"
                    + " ORDER BY e." + AttendBleDbHelper.C_E_DATE + " DESC";
            try (Cursor c = db.rawQuery(sql, new String[]{etudiantId})) {
                while (c.moveToNext()) {
                    Classe k = readClasse(c);
                    int[] stats = computeStudentAttendance(db, k.getClasseId(), etudiantId);
                    result.add(new ClasseWithAttendance(k, stats[0], stats[1]));
                }
            }
            return result;
        }, callback);
    }

    /** [tauxMoyen, nbSessionsFermees] pour la classe — moyenne sur tous les inscrits. */
    private int[] computeAvgAttendance(SQLiteDatabase db, String classeId) {
        int nbSessions;
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_SESSIONS
                        + " WHERE " + AttendBleDbHelper.C_S_CLASSE_ID + " = ?"
                        + " AND " + AttendBleDbHelper.C_S_STATUT + " = ?",
                new String[]{classeId, SessionStatus.FERMEE.name()})) {
            nbSessions = c.moveToFirst() ? c.getInt(0) : 0;
        }
        if (nbSessions == 0) return new int[]{100, 0};

        int nbInscrits;
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_ENROLLMENTS
                        + " WHERE " + AttendBleDbHelper.C_E_CLASSE_ID + " = ?",
                new String[]{classeId})) {
            nbInscrits = c.moveToFirst() ? c.getInt(0) : 0;
        }
        if (nbInscrits == 0) return new int[]{100, nbSessions};

        int nbPresences;
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_POINTAGES + " p"
                        + " INNER JOIN " + AttendBleDbHelper.T_SESSIONS + " s"
                        + "   ON p." + AttendBleDbHelper.C_P_SESSION_ID + " = s." + AttendBleDbHelper.C_S_SESSION_ID
                        + " WHERE s." + AttendBleDbHelper.C_S_CLASSE_ID + " = ?"
                        + "   AND p." + AttendBleDbHelper.C_P_STATUT + " = ?"
                        + "   AND s." + AttendBleDbHelper.C_S_STATUT + " = ?",
                new String[]{classeId, PointageStatut.PRESENT.name(), SessionStatus.FERMEE.name()})) {
            nbPresences = c.moveToFirst() ? c.getInt(0) : 0;
        }
        int taux = Math.round(100f * nbPresences / (nbInscrits * nbSessions));
        return new int[]{taux, nbSessions};
    }

    /** [tauxEtudiant, nbSessionsFermees] pour cet étudiant dans cette classe. */
    private int[] computeStudentAttendance(SQLiteDatabase db, String classeId, String etudiantId) {
        int nbSessions;
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_SESSIONS
                        + " WHERE " + AttendBleDbHelper.C_S_CLASSE_ID + " = ?"
                        + " AND " + AttendBleDbHelper.C_S_STATUT + " = ?",
                new String[]{classeId, SessionStatus.FERMEE.name()})) {
            nbSessions = c.moveToFirst() ? c.getInt(0) : 0;
        }
        if (nbSessions == 0) return new int[]{100, 0};

        int nbPresences;
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AttendBleDbHelper.T_POINTAGES + " p"
                        + " INNER JOIN " + AttendBleDbHelper.T_SESSIONS + " s"
                        + "   ON p." + AttendBleDbHelper.C_P_SESSION_ID + " = s." + AttendBleDbHelper.C_S_SESSION_ID
                        + " WHERE s." + AttendBleDbHelper.C_S_CLASSE_ID + " = ?"
                        + "   AND p." + AttendBleDbHelper.C_P_ETUDIANT_ID + " = ?"
                        + "   AND p." + AttendBleDbHelper.C_P_STATUT + " = ?"
                        + "   AND s." + AttendBleDbHelper.C_S_STATUT + " = ?",
                new String[]{classeId, etudiantId, PointageStatut.PRESENT.name(), SessionStatus.FERMEE.name()})) {
            nbPresences = c.moveToFirst() ? c.getInt(0) : 0;
        }
        return new int[]{Math.round(100f * nbPresences / nbSessions), nbSessions};
    }

    private Classe lookupByCode(SQLiteDatabase db, String codeInvitation) {
        String normalized = codeInvitation == null ? "" : codeInvitation.trim().toUpperCase(Locale.ROOT);
        try (Cursor c = db.query(AttendBleDbHelper.T_CLASSES, null,
                AttendBleDbHelper.C_C_CODE_INVITATION + " = ?", new String[]{normalized},
                null, null, null)) {
            return c.moveToFirst() ? readClasse(c) : null;
        }
    }

    private Classe readClasse(Cursor c) {
        Classe k = new Classe();
        k.setClasseId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_CLASSE_ID)));
        k.setNom(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_NOM)));
        k.setMatiere(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_MATIERE)));
        k.setGroupe(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_GROUPE)));
        k.setSalle(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_SALLE)));
        k.setHoraire(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_HORAIRE)));
        k.setJourSemaine(c.getInt(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_JOUR_SEMAINE)));
        k.setHeureDebut(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_HEURE_DEBUT)));
        k.setHeureFin(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_HEURE_FIN)));
        k.setNbEtudiants(c.getInt(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_NB_ETUDIANTS)));
        k.setCodeInvitation(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_CODE_INVITATION)));
        k.setProfesseurId(c.getString(c.getColumnIndexOrThrow(AttendBleDbHelper.C_C_PROFESSEUR_ID)));
        return k;
    }

    private String generateUniqueCode(SQLiteDatabase db, String groupe, String nom) {
        String groupePart = sanitize(groupe, 4);
        String nomPart = sanitize(nom, 3);
        for (int i = 0; i < MAX_GENERATION_RETRIES; i++) {
            String code = groupePart + "-" + nomPart + "-" + randomSuffix();
            try (Cursor c = db.query(AttendBleDbHelper.T_CLASSES,
                    new String[]{AttendBleDbHelper.C_C_CLASSE_ID},
                    AttendBleDbHelper.C_C_CODE_INVITATION + " = ?",
                    new String[]{code}, null, null, null)) {
                if (!c.moveToFirst()) return code;
            }
        }
        return null;
    }

    private String sanitize(String input, int maxLen) {
        if (input == null) return "CLS";
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toUpperCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(ch)) sb.append(ch);
            if (sb.length() >= maxLen) break;
        }
        return sb.length() == 0 ? "CLS" : sb.toString();
    }

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(CODE_SUFFIX_LENGTH);
        for (int i = 0; i < CODE_SUFFIX_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
