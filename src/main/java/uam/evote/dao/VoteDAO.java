package uam.evote.dao;

import uam.evote.model.Vote;

import java.util.List;
import java.util.Map;

public interface VoteDAO {

    // Enregistre un vote. Retourne false si l'utilisateur a déjà voté à cette élection.
    boolean enregistrer(Vote vote);

    // Vérifie si un utilisateur a déjà voté à une élection donnée
    boolean aDejaVote(int electionId, int utilisateurId);

    // Compte le nombre total de votes pour une élection
    int compterVotes(int electionId);

    // Résultats du dépouillement : id du candidat -> nombre de votes reçus
    // (Aucune donnée nominative ici : c'est ce qui garantit l'anonymat du résultat affiché)
    Map<Integer, Integer> compterVotesParCandidat(int electionId);

    List<Vote> trouverParElection(int electionId);
}