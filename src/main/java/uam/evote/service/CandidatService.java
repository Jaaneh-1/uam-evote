package uam.evote.service;

import uam.evote.dao.CandidatDAO;
import uam.evote.dao.CandidatDAOImpl;
import uam.evote.model.Candidat;

import java.util.List;
import java.util.Optional;

public class CandidatService {

    private final CandidatDAO candidatDAO;

    public CandidatService() {
        this.candidatDAO = new CandidatDAOImpl();
    }

    public int ajouterCandidat(Candidat candidat) {
        if (candidat.getNom() == null || candidat.getNom().isBlank()
                || candidat.getPrenom() == null || candidat.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le nom et le prénom du candidat sont obligatoires.");
        }
        return candidatDAO.ajouter(candidat);
    }

    public void modifierCandidat(Candidat candidat) {
        candidatDAO.modifier(candidat);
    }

    public void supprimerCandidat(int id) {
        candidatDAO.supprimer(id);
    }

    public List<Candidat> listerParElection(int electionId) {
        return candidatDAO.trouverParElection(electionId);
    }

    public Optional<Candidat> trouverParId(int id) {
        return candidatDAO.trouverParId(id);
    }
}