package uam.evote.model;

import java.time.LocalDateTime;

public class Vote {

    private int id;
    private int electionId;
    private int candidatId;
    private int utilisateurId; // sert uniquement à empêcher le double vote (contrainte UNIQUE en base)
    private LocalDateTime dateVote;

    public Vote() {
    }

    public Vote(int electionId, int candidatId, int utilisateurId) {
        this.electionId = electionId;
        this.candidatId = candidatId;
        this.utilisateurId = utilisateurId;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public int getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(int candidatId) {
        this.candidatId = candidatId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public LocalDateTime getDateVote() {
        return dateVote;
    }

    public void setDateVote(LocalDateTime dateVote) {
        this.dateVote = dateVote;
    }
}