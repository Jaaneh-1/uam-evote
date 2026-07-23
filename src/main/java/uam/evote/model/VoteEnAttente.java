package uam.evote.model;

public class VoteEnAttente {

    private int electionId;
    private int candidatId;
    private int utilisateurId;
    private String dateVote; // stocké en texte simple pour rester léger

    public VoteEnAttente() {
    }

    public VoteEnAttente(int electionId, int candidatId, int utilisateurId, String dateVote) {
        this.electionId = electionId;
        this.candidatId = candidatId;
        this.utilisateurId = utilisateurId;
        this.dateVote = dateVote;
    }

    public int getElectionId() {
        return electionId;
    }

    public int getCandidatId() {
        return candidatId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public String getDateVote() {
        return dateVote;
    }
}