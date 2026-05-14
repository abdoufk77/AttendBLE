package com.example.attendble.domain.repository;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.model.EtudiantAttendance;

import java.util.List;

/**
 * Contrat de gestion des classes côté professeur et étudiant.
 * Implémentations : {@code SqliteClasseRepository} (actuel), {@code RetrofitClasseRepository} (futur).
 */
public interface ClasseRepository {

    /** Persiste la classe (id + codeInvitation déjà attribués par le use case/repo). */
    void creerClasse(Classe classe, Callback<Classe> callback);

    /** Liste les classes appartenant à un professeur. */
    void listClassesByProfesseur(String professeurId, Callback<List<Classe>> callback);

    /** Recherche une classe par son id (utilisé pour l'écran détails). */
    void findById(String classeId, Callback<Classe> callback);

    /** Recherche une classe via son code d'invitation (utilisé côté étudiant pour rejoindre). */
    void findByCodeInvitation(String codeInvitation, Callback<Classe> callback);

    /** Inscrit un étudiant à une classe via son code d'invitation. Retourne la classe rejointe. */
    void joinClasseByCode(String codeInvitation, String etudiantId, Callback<Classe> callback);

    /** Liste les classes dans lesquelles un étudiant est inscrit. */
    void listClassesByEtudiant(String etudiantId, Callback<List<Classe>> callback);

    /** Cours du jour pour un prof (filtre {@code jourSemaine}, tri par heureDebut). */
    void listTodayByProfesseur(String professeurId, int jourSemaine, Callback<List<Classe>> callback);

    /** Cours du jour pour un étudiant (filtre {@code jourSemaine}, tri par heureDebut). */
    void listTodayByEtudiant(String etudiantId, int jourSemaine, Callback<List<Classe>> callback);

    /** Étudiants inscrits à une classe + stats de présence (pour l'écran prof "détails classe"). */
    void listEtudiantsByClasse(String classeId, Callback<List<EtudiantAttendance>> callback);
}
