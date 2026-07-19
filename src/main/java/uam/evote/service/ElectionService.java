package uam.evote.service;

import uam.evote.dao.ElectionDAO;
import uam.evote.dao.ElectionDAOImpl;
import uam.evote.model.Election;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ElectionService {

    private final ElectionDAO electionDAO;

    public ElectionService() {
        this.electionDAO = new ElectionDAOImpl();
    }

    public int creerElection(Election election) {
        if (election.getDateFin().isBefore(election.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        election.setStatut(Election.Statut.EN_PREPARATION);
        return electionDAO.ajouter(election);
    }

    public void modifierElection(Election election) {
        electionDAO.modifier(election);
    }

    public void supprimerElection(int id) {
        electionDAO.supprimer(id);
    }

    public List<Election> listerToutes() {
        return electionDAO.trouverToutes();
    }

    public Optional<Election> trouverParId(int id) {
        return electionDAO.trouverParId(id);
    }

    /**
     * Ouvre le scrutin : passe l'élection de EN_PREPARATION à OUVERTE.
     */
    public void ouvrirScrutin(int electionId) {
        Election election = electionDAO.trouverParId(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Élection introuvable."));

        if (election.getStatut() != Election.Statut.EN_PREPARATION) {
            throw new IllegalStateException("Seule une élection en préparation peut être ouverte.");
        }
        electionDAO.changerStatut(electionId, Election.Statut.OUVERTE);
    }

    /**
     * Ferme le scrutin : passe l'élection de OUVERTE à FERMEE.
     * C'est le déclencheur logique du dépouillement (calculé à la demande, pas stocké).
     */
    public void fermerScrutin(int electionId) {
        Election election = electionDAO.trouverParId(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Élection introuvable."));

        if (election.getStatut() != Election.Statut.OUVERTE) {
            throw new IllegalStateException("Seule une élection ouverte peut être fermée.");
        }
        electionDAO.changerStatut(electionId, Election.Statut.FERMEE);
    }

    /**
     * Historique : toutes les élections dont le scrutin est terminé (statut FERMEE),
     * triées de la plus récente à la plus ancienne.
     */
    public List<Election> listerHistorique() {
        return electionDAO.trouverParStatut(Election.Statut.FERMEE).stream()
                .sorted((a, b) -> b.getDateFin().compareTo(a.getDateFin()))
                .toList();
    }
}