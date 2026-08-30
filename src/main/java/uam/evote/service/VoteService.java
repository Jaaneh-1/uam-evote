package uam.evote.service;

import uam.evote.dao.*;
import uam.evote.model.Candidat;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.model.Vote;

import java.util.List;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Map;

public class VoteService {

    private final ElectionDAO electionDAO;
    private final CandidatDAO candidatDAO;
    private final VoteDAO voteDAO;

    public VoteService() {
        this.electionDAO = new ElectionDAOImpl();
        this.candidatDAO = new CandidatDAOImpl();
        this.voteDAO = new VoteDAOImpl();
    }

    /**
     * Retourne toutes les élections actuellement OUVERTES où l'utilisateur donné
     * est éligible pour voter (Cas 1/2/3 du sujet, via Election.estEligible()).
     */
    public List<Election> listerElectionsEligibles(User user) {
        return electionDAO.trouverParStatut(Election.Statut.OUVERTE).stream()
                .filter(election -> election.estEligible(user))
                .collect(Collectors.toList());
    }

    /**
     * Retourne toutes les élections FERMEES où l'utilisateur était éligible pour voter,
     * afin qu'il puisse consulter les résultats publiés (exigence du cahier des charges).
     */
    public List<Election> listerElectionsTermineesEligibles(User user) {
        return electionDAO.trouverParStatut(Election.Statut.FERMEE).stream()
                .filter(election -> election.estEligible(user))
                .collect(Collectors.toList());
    }


    public boolean aDejaVote(int electionId, int utilisateurId) {
        return voteDAO.aDejaVote(electionId, utilisateurId);
    }

    public List<Candidat> listerCandidats(int electionId) {
        uam.evote.utils.CandidatCache cache = new uam.evote.utils.CandidatCache();
        try {
            List<Candidat> candidats = candidatDAO.trouverParElection(electionId);
            cache.sauvegarder(electionId, candidats);
            return candidats;
        } catch (RuntimeException e) {
            List<Candidat> candidatsEnCache = cache.listerParElection(electionId);
            if (candidatsEnCache.isEmpty()) {
                throw e;
            }
            return candidatsEnCache;
        }
    }

    /**
     * Enregistre le vote d'un électeur, après re-vérification complète de toutes les règles.
     * On ne fait JAMAIS confiance uniquement à l'interface : chaque règle est revérifiée ici,
     * côté serveur/logique métier, avant d'enregistrer quoi que ce soit.
     */
    public void voter(User electeur, int electionId, int candidatId) {
        Election election = electionDAO.trouverParId(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Élection introuvable."));

        if (election.getStatut() != Election.Statut.OUVERTE) {
            throw new IllegalStateException("Cette élection n'est pas ouverte au vote.");
        }

        if (!election.estEligible(electeur)) {
            throw new IllegalStateException("Vous n'êtes pas éligible pour voter à cette élection.");
        }

        if (voteDAO.aDejaVote(electionId, electeur.getId())) {
            throw new IllegalStateException("Vous avez déjà voté à cette élection.");
        }

        Vote vote = new Vote(electionId, candidatId, electeur.getId());
        boolean succes = voteDAO.enregistrer(vote);

        if (!succes) {
            // Filet de sécurité ultime : la contrainte UNIQUE en base a bloqué
            // un double vote qui aurait échappé aux vérifications ci-dessus
            // (ex: deux clics rapides, ou double connexion simultanée)
            throw new IllegalStateException("Vous avez déjà voté à cette élection.");
        }
    }

    /**
     * Calcule le nombre d'électeurs éligibles à une élection donnée,
     * en filtrant tous les utilisateurs avec Election.estEligible().
     */
    public int compterElecteursEligibles(Election election, List<User> tousLesUtilisateurs) {
        return (int) tousLesUtilisateurs.stream()
                .filter(u -> u.getRole() != User.Role.ADMIN) // un admin ne vote jamais
                .filter(election::estEligible)
                .count();
    }

    /**
     * Résultats du dépouillement, avec le nom des candidats déjà résolu
     * (pratique pour l'affichage), triés du plus de voix au moins de voix.
     * Reste 100% anonyme : aucune donnée d'électeur n'entre dans ce calcul.
     */
    public List<ResultatCandidat> calculerResultats(int electionId) {
        Map<Integer, Integer> votesParCandidat = voteDAO.compterVotesParCandidat(electionId);
        List<Candidat> candidats = candidatDAO.trouverParElection(electionId);

        List<ResultatCandidat> resultats = new ArrayList<>();
        for (Candidat candidat : candidats) {
            int nbVotes = votesParCandidat.getOrDefault(candidat.getId(), 0);
            resultats.add(new ResultatCandidat(candidat, nbVotes));
        }

        resultats.sort((a, b) -> Integer.compare(b.getNbVotes(), a.getNbVotes()));
        return resultats;
    }

    /**
     * Petite classe interne : associe un candidat à son nombre de votes,
     * pour simplifier l'affichage dans l'interface (tableau de bord, graphiques).
     */
    public static class ResultatCandidat {
        private final Candidat candidat;
        private final int nbVotes;

        public ResultatCandidat(Candidat candidat, int nbVotes) {
            this.candidat = candidat;
            this.nbVotes = nbVotes;
        }

        public Candidat getCandidat() {
            return candidat;
        }

        public int getNbVotes() {
            return nbVotes;
        }
    }
}