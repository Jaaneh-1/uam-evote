package uam.evote.service;

import uam.evote.dao.*;
import uam.evote.model.Candidat;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.model.Vote;

import java.util.List;
import java.util.stream.Collectors;

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

    public boolean aDejaVote(int electionId, int utilisateurId) {
        return voteDAO.aDejaVote(electionId, utilisateurId);
    }

    public List<Candidat> listerCandidats(int electionId) {
        return candidatDAO.trouverParElection(electionId);
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
}