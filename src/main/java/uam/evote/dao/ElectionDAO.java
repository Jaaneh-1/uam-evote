package uam.evote.dao;

import uam.evote.model.Election;

import java.util.List;
import java.util.Optional;

public interface ElectionDAO {

    int ajouter(Election election);

    void modifier(Election election);

    void supprimer(int id);

    Optional<Election> trouverParId(int id);

    List<Election> trouverToutes();

    // Élections ayant un statut donné (ex: toutes les "OUVERTE")
    List<Election> trouverParStatut(Election.Statut statut);

    // Change uniquement le statut d'une élection (ex: passer de EN_PREPARATION à OUVERTE)
    void changerStatut(int id, Election.Statut nouveauStatut);
}