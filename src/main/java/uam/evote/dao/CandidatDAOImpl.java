package uam.evote.dao;

import uam.evote.model.Candidat;
import uam.evote.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CandidatDAOImpl implements CandidatDAO {

    @Override
    public int ajouter(Candidat candidat) {
        String sql = "INSERT INTO candidats (election_id, user_id, nom, prenom, photo, profession, programme) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, candidat.getElectionId());
            ps.setInt(2, candidat.getUserId());
            ps.setString(3, candidat.getNom());
            ps.setString(4, candidat.getPrenom());
            ps.setString(5, candidat.getPhoto());
            ps.setString(6, candidat.getProfession());
            ps.setString(7, candidat.getProgramme());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Cet utilisateur est déjà candidat à cette élection.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du candidat", e);
        }
    }

    @Override
    public void modifier(Candidat candidat) {
        String sql = "UPDATE candidats SET user_id=?, nom=?, prenom=?, photo=?, profession=?, programme=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidat.getUserId());
            ps.setString(2, candidat.getNom());
            ps.setString(3, candidat.getPrenom());
            ps.setString(4, candidat.getPhoto());
            ps.setString(5, candidat.getProfession());
            ps.setString(6, candidat.getProgramme());
            ps.setInt(7, candidat.getId());

            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Cet utilisateur est déjà candidat à cette élection.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du candidat", e);
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM candidats WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du candidat", e);
        }
    }

    @Override
    public Optional<Candidat> trouverParId(int id) {
        String sql = "SELECT * FROM candidats WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCandidat(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du candidat", e);
        }
    }

    @Override
    public List<Candidat> trouverParElection(int electionId) {
        String sql = "SELECT * FROM candidats WHERE election_id = ?";
        List<Candidat> candidats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, electionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    candidats.add(mapResultSetToCandidat(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des candidats", e);
        }
        return candidats;
    }

    private Candidat mapResultSetToCandidat(ResultSet rs) throws SQLException {
        Candidat candidat = new Candidat();
        candidat.setId(rs.getInt("id"));
        candidat.setElectionId(rs.getInt("election_id"));
        candidat.setUserId(rs.getInt("user_id"));
        candidat.setNom(rs.getString("nom"));
        candidat.setPrenom(rs.getString("prenom"));
        candidat.setPhoto(rs.getString("photo"));
        candidat.setProfession(rs.getString("profession"));
        candidat.setProgramme(rs.getString("programme"));
        return candidat;
    }
}