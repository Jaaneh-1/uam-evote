package uam.evote.dao;

import uam.evote.model.Vote;
import uam.evote.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VoteDAOImpl implements VoteDAO {

    @Override
    public boolean enregistrer(Vote vote) {
        // On revérifie ICI, côté base, même si le Service aura déjà vérifié avant.
        // C'est la contrainte UNIQUE(election_id, utilisateur_id) en base qui est
        // la vraie garantie ultime contre le double vote (double sécurité).
        String sql = "INSERT INTO votes (election_id, candidat_id, utilisateur_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vote.getElectionId());
            ps.setInt(2, vote.getCandidatId());
            ps.setInt(3, vote.getUtilisateurId());

            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Se déclenche automatiquement si l'utilisateur a déjà voté
            // (violation de la contrainte UNIQUE définie dans le schéma SQL)
            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du vote", e);
        }
    }

    @Override
    public boolean aDejaVote(int electionId, int utilisateurId) {
        String sql = "SELECT COUNT(*) FROM votes WHERE election_id = ? AND utilisateur_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, electionId);
            ps.setInt(2, utilisateurId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du vote", e);
        }
    }

    @Override
    public int compterVotes(int electionId) {
        String sql = "SELECT COUNT(*) FROM votes WHERE election_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, electionId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des votes", e);
        }
    }

    @Override
    public Map<Integer, Integer> compterVotesParCandidat(int electionId) {
        // Requête volontairement anonyme : on ne sélectionne QUE candidat_id et un COUNT.
        // Aucune jointure avec la table users ici -> impossible de savoir qui a voté pour qui.
        String sql = "SELECT candidat_id, COUNT(*) as nb FROM votes " +
                "WHERE election_id = ? GROUP BY candidat_id ORDER BY nb DESC";

        Map<Integer, Integer> resultats = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, electionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultats.put(rs.getInt("candidat_id"), rs.getInt("nb"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul des résultats", e);
        }
        return resultats;
    }

    @Override
    public List<Vote> trouverParElection(int electionId) {
        String sql = "SELECT * FROM votes WHERE election_id = ?";
        List<Vote> votes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, electionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vote vote = new Vote();
                    vote.setId(rs.getInt("id"));
                    vote.setElectionId(rs.getInt("election_id"));
                    vote.setCandidatId(rs.getInt("candidat_id"));
                    vote.setUtilisateurId(rs.getInt("utilisateur_id"));
                    vote.setDateVote(rs.getTimestamp("date_vote").toLocalDateTime());
                    votes.add(vote);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des votes", e);
        }
        return votes;
    }
}