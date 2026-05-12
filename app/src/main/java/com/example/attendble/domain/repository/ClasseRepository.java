package com.example.attendble.domain.repository;

import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;

import java.util.List;

/**
 * Contrat de gestion des classes côté professeur et étudiant.
 * Implémentations : {@code InMemoryClasseRepository} (actuel), {@code FirebaseClasseRepository} (futur).
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
}
