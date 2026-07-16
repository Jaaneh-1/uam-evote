package uam.evote.dao;

import uam.evote.model.Candidat;

import java.util.List;
import java.util.Optional;

public interface CandidatDAO {

    int ajouter(Candidat candidat);

    void modifier(Candidat candidat);

    void supprimer(int id);

    Optional<Candidat> trouverParId(int id);

    // Tous les candidats d'une élection donnée (le cas d'usage principal)
    List<Candidat> trouverParElection(int electionId);
}