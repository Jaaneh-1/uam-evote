package uam.evote.service;

import uam.evote.dao.UserDAO;
import uam.evote.dao.UserDAOImpl;
import uam.evote.model.User;
import uam.evote.model.VoteEnAttente;
import uam.evote.utils.OfflineVoteQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VoteSyncService {

    private final OfflineVoteQueue queue = new OfflineVoteQueue();
    private final VoteService voteService = new VoteService();
    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * Tente de pousser vers le serveur tous les votes enregistrés en mode hors ligne.
     * Chaque vote repasse par TOUTES les vérifications normales de VoteService.voter()
     * (éligibilité, élection ouverte, pas déjà voté) — le mode hors ligne ne contourne
     * jamais les règles métier, il ne fait que reporter leur vérification.
     */
    public SyncRapport synchroniser() {
        SyncRapport rapport = new SyncRapport();
        List<VoteEnAttente> enAttente = queue.lister();
        List<VoteEnAttente> traites = new ArrayList<>();

        for (VoteEnAttente v : enAttente) {
            Optional<User> electeurOpt = userDAO.trouverParId(v.getUtilisateurId());

            if (electeurOpt.isEmpty()) {
                rapport.ajouterEchec(v, "Électeur introuvable (id " + v.getUtilisateurId() + ")");
                traites.add(v); // définitif, inutile de réessayer
                continue;
            }

            try {
                voteService.voter(electeurOpt.get(), v.getElectionId(), v.getCandidatId());
                rapport.ajouterSucces(v, electeurOpt.get());
                traites.add(v);

            } catch (IllegalStateException e) {
                // Rejet définitif (déjà voté, élection fermée...) : on retire de la file,
                // ce n'est pas un problème de connexion, réessayer ne changerait rien.
                rapport.ajouterEchec(v, e.getMessage());
                traites.add(v);

            } catch (RuntimeException e) {
                // Probable souci de connexion encore présent : on garde ce vote en attente
                rapport.ajouterEnAttente(v);
            }
        }

        queue.retirer(traites);
        return rapport;
    }

    public int compterVotesEnAttente() {
        return queue.compter();
    }

    public static class SyncRapport {
        private final List<String> succes = new ArrayList<>();
        private final List<String> echecs = new ArrayList<>();
        private int nbEncoreEnAttente = 0;

        public void ajouterSucces(VoteEnAttente v, User electeur) {
            succes.add(electeur.getNomComplet() + " (élection " + v.getElectionId() + ")");
        }

        public void ajouterEchec(VoteEnAttente v, String raison) {
            echecs.add("Utilisateur id " + v.getUtilisateurId() + " : " + raison);
        }

        public void ajouterEnAttente(VoteEnAttente v) {
            nbEncoreEnAttente++;
        }

        public List<String> getSucces() {
            return succes;
        }

        public List<String> getEchecs() {
            return echecs;
        }

        public int getNbEncoreEnAttente() {
            return nbEncoreEnAttente;
        }
    }
}