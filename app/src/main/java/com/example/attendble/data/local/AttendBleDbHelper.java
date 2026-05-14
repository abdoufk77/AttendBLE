package com.example.attendble.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.attendble.domain.enums.PointageStatut;
import com.example.attendble.domain.enums.SessionStatus;
import com.example.attendble.domain.enums.UserRole;

import java.util.UUID;

/**
 * Schéma SQLite local + données de démonstration injectées à la création de la base.
 * Tables : users, classes, enrollments, sessions, pointages.
 * Permet de lancer l'app avec des comptes/classes/historique déjà peuplés sans backend.
 */
public class AttendBleDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "attendble.db";
    public static final int DB_VERSION = 2;

    // users
    public static final String T_USERS = "users";
    public static final String C_U_UID = "uid";
    public static final String C_U_EMAIL = "email";
    public static final String C_U_NOM = "nom";
    public static final String C_U_PHOTO = "photo_url";
    public static final String C_U_ROLE = "role";
    public static final String C_U_PASSWORD = "password";
    public static final String C_U_DEPARTMENT = "department";
    public static final String C_U_NUM_ETUD = "num_etud";
    public static final String C_U_FACE_EMBEDDING = "face_embedding";
    public static final String C_U_DATE_INSCRIPTION = "date_inscription";

    // classes
    public static final String T_CLASSES = "classes";
    public static final String C_C_CLASSE_ID = "classe_id";
    public static final String C_C_NOM = "nom";
    public static final String C_C_MATIERE = "matiere";
    public static final String C_C_GROUPE = "groupe";
    public static final String C_C_SALLE = "salle";
    public static final String C_C_HORAIRE = "horaire";
    public static final String C_C_JOUR_SEMAINE = "jour_semaine";
    public static final String C_C_HEURE_DEBUT = "heure_debut";
    public static final String C_C_HEURE_FIN = "heure_fin";
    public static final String C_C_NB_ETUDIANTS = "nb_etudiants";
    public static final String C_C_CODE_INVITATION = "code_invitation";
    public static final String C_C_PROFESSEUR_ID = "professeur_id";
    public static final String C_C_DATE_CREATION = "date_creation";

    // enrollments
    public static final String T_ENROLLMENTS = "enrollments";
    public static final String C_E_CLASSE_ID = "classe_id";
    public static final String C_E_ETUDIANT_ID = "etudiant_id";
    public static final String C_E_DATE = "date_inscription";

    // sessions
    public static final String T_SESSIONS = "sessions";
    public static final String C_S_SESSION_ID = "session_id";
    public static final String C_S_CLASSE_ID = "classe_id";
    public static final String C_S_CODE_TEMP = "code_temp";
    public static final String C_S_CODE_EXPIRE_AT = "code_expire_at";
    public static final String C_S_BEACON_UUID = "beacon_uuid";
    public static final String C_S_STATUT = "statut";
    public static final String C_S_DATE_OUVERTURE = "date_ouverture";
    public static final String C_S_DATE_FERMETURE = "date_fermeture";

    // pointages
    public static final String T_POINTAGES = "pointages";
    public static final String C_P_POINTAGE_ID = "pointage_id";
    public static final String C_P_SESSION_ID = "session_id";
    public static final String C_P_ETUDIANT_ID = "etudiant_id";
    public static final String C_P_HEURE = "heure_pointage";
    public static final String C_P_STATUT = "statut";
    public static final String C_P_BLE_DETECTE = "ble_detecte";
    public static final String C_P_FACE_VERIFIED = "face_verified";

    private static AttendBleDbHelper INSTANCE;

    /** Singleton — un seul SQLiteOpenHelper pour toute l'app pour éviter les locks. */
    public static synchronized AttendBleDbHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AttendBleDbHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private AttendBleDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + " ("
                + C_U_UID + " TEXT PRIMARY KEY,"
                + C_U_EMAIL + " TEXT NOT NULL UNIQUE,"
                + C_U_NOM + " TEXT NOT NULL,"
                + C_U_PHOTO + " TEXT,"
                + C_U_ROLE + " TEXT NOT NULL,"
                + C_U_PASSWORD + " TEXT NOT NULL,"
                + C_U_DEPARTMENT + " TEXT,"
                + C_U_NUM_ETUD + " TEXT,"
                + C_U_FACE_EMBEDDING + " BLOB,"
                + C_U_DATE_INSCRIPTION + " INTEGER NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + T_CLASSES + " ("
                + C_C_CLASSE_ID + " TEXT PRIMARY KEY,"
                + C_C_NOM + " TEXT NOT NULL,"
                + C_C_MATIERE + " TEXT,"
                + C_C_GROUPE + " TEXT,"
                + C_C_SALLE + " TEXT,"
                + C_C_HORAIRE + " TEXT,"
                + C_C_JOUR_SEMAINE + " INTEGER NOT NULL DEFAULT 1,"
                + C_C_HEURE_DEBUT + " TEXT,"
                + C_C_HEURE_FIN + " TEXT,"
                + C_C_NB_ETUDIANTS + " INTEGER NOT NULL DEFAULT 0,"
                + C_C_CODE_INVITATION + " TEXT NOT NULL UNIQUE,"
                + C_C_PROFESSEUR_ID + " TEXT NOT NULL,"
                + C_C_DATE_CREATION + " INTEGER NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + T_ENROLLMENTS + " ("
                + C_E_CLASSE_ID + " TEXT NOT NULL,"
                + C_E_ETUDIANT_ID + " TEXT NOT NULL,"
                + C_E_DATE + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + C_E_CLASSE_ID + "," + C_E_ETUDIANT_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + T_SESSIONS + " ("
                + C_S_SESSION_ID + " TEXT PRIMARY KEY,"
                + C_S_CLASSE_ID + " TEXT NOT NULL,"
                + C_S_CODE_TEMP + " TEXT NOT NULL,"
                + C_S_CODE_EXPIRE_AT + " INTEGER NOT NULL,"
                + C_S_BEACON_UUID + " TEXT NOT NULL,"
                + C_S_STATUT + " TEXT NOT NULL,"
                + C_S_DATE_OUVERTURE + " INTEGER NOT NULL,"
                + C_S_DATE_FERMETURE + " INTEGER"
                + ")");
        db.execSQL("CREATE INDEX idx_sessions_classe ON " + T_SESSIONS + "(" + C_S_CLASSE_ID + ")");

        db.execSQL("CREATE TABLE " + T_POINTAGES + " ("
                + C_P_POINTAGE_ID + " TEXT PRIMARY KEY,"
                + C_P_SESSION_ID + " TEXT NOT NULL,"
                + C_P_ETUDIANT_ID + " TEXT NOT NULL,"
                + C_P_HEURE + " INTEGER NOT NULL,"
                + C_P_STATUT + " TEXT NOT NULL,"
                + C_P_BLE_DETECTE + " INTEGER NOT NULL DEFAULT 0,"
                + C_P_FACE_VERIFIED + " INTEGER NOT NULL DEFAULT 0,"
                + "UNIQUE (" + C_P_SESSION_ID + "," + C_P_ETUDIANT_ID + ")"
                + ")");

        seedDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_POINTAGES);
        db.execSQL("DROP TABLE IF EXISTS " + T_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + T_ENROLLMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }

    /**
     * Peuple la DB avec un jeu de démo cohérent :
     * 1 prof, 3 étudiants, 2 classes, inscriptions croisées, 1 session passée FERMEE avec pointages.
     */
    private void seedDemoData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        long dayMs = 24L * 60 * 60 * 1000;

        // --- Users ---
        String profId = "prof-hassan-0001";
        insertUser(db, profId, "prof@gmail.com", "hassan", UserRole.PROFESSEUR, "proof7",
                "info", null, now - 30 * dayMs);

        String abdouId = "etud-abdou-0001";
        insertUser(db, abdouId, "ab.foukahy@gmail.com", "abdou", UserRole.ETUDIANT, "abdou7",
                null, "123456", now - 25 * dayMs);

        String saraId = "etud-sara-0002";
        insertUser(db, saraId, "sara@gmail.com", "sara", UserRole.ETUDIANT, "sara7",
                null, "123457", now - 20 * dayMs);

        String youssefId = "etud-youssef-0003";
        insertUser(db, youssefId, "youssef@gmail.com", "youssef", UserRole.ETUDIANT, "youssef7",
                null, "123458", now - 18 * dayMs);

        // --- Classes (6 cours répartis sur la semaine, ISO 1=Lun..7=Dim) ---
        // Aujourd'hui (2026-05-14) est un Jeudi → 2 cours le jeudi pour démo "today's schedule".
        String classeAlgoId = "classe-algo-0001";
        insertClasse(db, classeAlgoId, "Algorithmique", "Informatique", "GL3", "B204",
                4, "10:00", "12:00", 3, "GL3-ALG-7X2", profId, now - 15 * dayMs);

        String classeReseauxId = "classe-reseaux-0002";
        insertClasse(db, classeReseauxId, "Réseaux", "Informatique", "GL3", "A101",
                3, "14:00", "16:00", 3, "GL3-RES-K3B", profId, now - 10 * dayMs);

        String classeBddId = "classe-bdd-0003";
        insertClasse(db, classeBddId, "Bases de données", "Informatique", "GL3", "C302",
                4, "14:00", "16:00", 3, "GL3-BDD-M4P", profId, now - 9 * dayMs);

        String classeProgWebId = "classe-progweb-0004";
        insertClasse(db, classeProgWebId, "Programmation Web", "Informatique", "GL3", "B210",
                2, "08:30", "10:30", 2, "GL3-WEB-Q7N", profId, now - 8 * dayMs);

        String classeMathId = "classe-math-0005";
        insertClasse(db, classeMathId, "Mathématiques", "Maths", "GL3", "A205",
                1, "08:00", "10:00", 3, "GL3-MAT-9LK", profId, now - 7 * dayMs);

        String classeIaId = "classe-ia-0006";
        insertClasse(db, classeIaId, "Intelligence Artificielle", "Informatique", "GL3", "D101",
                5, "10:00", "12:00", 2, "GL3-IA-X8R", profId, now - 5 * dayMs);

        // --- Enrollments ---
        insertEnrollment(db, classeAlgoId, abdouId, now - 14 * dayMs);
        insertEnrollment(db, classeAlgoId, saraId, now - 14 * dayMs);
        insertEnrollment(db, classeAlgoId, youssefId, now - 13 * dayMs);
        insertEnrollment(db, classeReseauxId, abdouId, now - 9 * dayMs);
        insertEnrollment(db, classeReseauxId, saraId, now - 9 * dayMs);
        insertEnrollment(db, classeReseauxId, youssefId, now - 9 * dayMs);
        insertEnrollment(db, classeBddId, abdouId, now - 8 * dayMs);
        insertEnrollment(db, classeBddId, saraId, now - 8 * dayMs);
        insertEnrollment(db, classeBddId, youssefId, now - 8 * dayMs);
        insertEnrollment(db, classeProgWebId, abdouId, now - 7 * dayMs);
        insertEnrollment(db, classeProgWebId, saraId, now - 7 * dayMs);
        insertEnrollment(db, classeMathId, abdouId, now - 6 * dayMs);
        insertEnrollment(db, classeMathId, saraId, now - 6 * dayMs);
        insertEnrollment(db, classeMathId, youssefId, now - 6 * dayMs);
        insertEnrollment(db, classeIaId, abdouId, now - 4 * dayMs);
        insertEnrollment(db, classeIaId, youssefId, now - 4 * dayMs);

        // --- Session passée (FERMEE) sur la classe Algo, semaine dernière ---
        String sessionPasseeId = "session-algo-past-0001";
        long sessionStart = now - 7 * dayMs;
        long sessionEnd = sessionStart + 2 * 60 * 60 * 1000; // 2h plus tard
        insertSession(db, sessionPasseeId, classeAlgoId, "7342", sessionStart + 2 * 60 * 1000,
                UUID.randomUUID().toString(), SessionStatus.FERMEE, sessionStart, sessionEnd);

        // Pointages : abdou + sara présents, youssef absent
        insertPointage(db, UUID.randomUUID().toString(), sessionPasseeId, abdouId,
                sessionStart + 5 * 60 * 1000, PointageStatut.PRESENT, true, true);
        insertPointage(db, UUID.randomUUID().toString(), sessionPasseeId, saraId,
                sessionStart + 8 * 60 * 1000, PointageStatut.PRESENT, true, true);
        insertPointage(db, UUID.randomUUID().toString(), sessionPasseeId, youssefId,
                sessionEnd, PointageStatut.ABSENT, false, false);
    }

    private void insertUser(SQLiteDatabase db, String uid, String email, String nom,
                            UserRole role, String password, String department,
                            String numEtud, long dateInscription) {
        ContentValues v = new ContentValues();
        v.put(C_U_UID, uid);
        v.put(C_U_EMAIL, email);
        v.put(C_U_NOM, nom);
        v.put(C_U_ROLE, role.name());
        v.put(C_U_PASSWORD, password);
        v.put(C_U_DEPARTMENT, department);
        v.put(C_U_NUM_ETUD, numEtud);
        v.put(C_U_DATE_INSCRIPTION, dateInscription);
        db.insert(T_USERS, null, v);
    }

    private void insertClasse(SQLiteDatabase db, String classeId, String nom, String matiere,
                              String groupe, String salle, int jourSemaine,
                              String heureDebut, String heureFin, int nbEtudiants,
                              String codeInvitation, String professeurId, long dateCreation) {
        ContentValues v = new ContentValues();
        v.put(C_C_CLASSE_ID, classeId);
        v.put(C_C_NOM, nom);
        v.put(C_C_MATIERE, matiere);
        v.put(C_C_GROUPE, groupe);
        v.put(C_C_SALLE, salle);
        v.put(C_C_HORAIRE, formatHoraire(jourSemaine, heureDebut, heureFin));
        v.put(C_C_JOUR_SEMAINE, jourSemaine);
        v.put(C_C_HEURE_DEBUT, heureDebut);
        v.put(C_C_HEURE_FIN, heureFin);
        v.put(C_C_NB_ETUDIANTS, nbEtudiants);
        v.put(C_C_CODE_INVITATION, codeInvitation);
        v.put(C_C_PROFESSEUR_ID, professeurId);
        v.put(C_C_DATE_CREATION, dateCreation);
        db.insert(T_CLASSES, null, v);
    }

    private static final String[] JOURS = {
            "", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
    };

    /** Format "Jeudi 10:00 - 12:00" pour affichage. */
    public static String formatHoraire(int jourSemaine, String heureDebut, String heureFin) {
        String jour = (jourSemaine >= 1 && jourSemaine <= 7) ? JOURS[jourSemaine] : "";
        return jour + " " + heureDebut + " - " + heureFin;
    }

    private void insertEnrollment(SQLiteDatabase db, String classeId, String etudiantId, long date) {
        ContentValues v = new ContentValues();
        v.put(C_E_CLASSE_ID, classeId);
        v.put(C_E_ETUDIANT_ID, etudiantId);
        v.put(C_E_DATE, date);
        db.insert(T_ENROLLMENTS, null, v);
    }

    private void insertSession(SQLiteDatabase db, String sessionId, String classeId, String codeTemp,
                               long codeExpireAt, String beaconUUID, SessionStatus statut,
                               long dateOuverture, Long dateFermeture) {
        ContentValues v = new ContentValues();
        v.put(C_S_SESSION_ID, sessionId);
        v.put(C_S_CLASSE_ID, classeId);
        v.put(C_S_CODE_TEMP, codeTemp);
        v.put(C_S_CODE_EXPIRE_AT, codeExpireAt);
        v.put(C_S_BEACON_UUID, beaconUUID);
        v.put(C_S_STATUT, statut.name());
        v.put(C_S_DATE_OUVERTURE, dateOuverture);
        v.put(C_S_DATE_FERMETURE, dateFermeture);
        db.insert(T_SESSIONS, null, v);
    }

    private void insertPointage(SQLiteDatabase db, String pointageId, String sessionId,
                                String etudiantId, long heure, PointageStatut statut,
                                boolean bleDetecte, boolean faceVerified) {
        ContentValues v = new ContentValues();
        v.put(C_P_POINTAGE_ID, pointageId);
        v.put(C_P_SESSION_ID, sessionId);
        v.put(C_P_ETUDIANT_ID, etudiantId);
        v.put(C_P_HEURE, heure);
        v.put(C_P_STATUT, statut.name());
        v.put(C_P_BLE_DETECTE, bleDetecte ? 1 : 0);
        v.put(C_P_FACE_VERIFIED, faceVerified ? 1 : 0);
        db.insert(T_POINTAGES, null, v);
    }
}
