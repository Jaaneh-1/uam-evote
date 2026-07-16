package uam.evote.dao;

import uam.evote.model.Election;
import uam.evote.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectionDAOImpl implements ElectionDAO {

    @Override
    public int ajouter(Election election) {
        String sql = "INSERT INTO elections (titre, type_election, cible_type, cible_valeur, " +
                "date_debut, date_fin, statut, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, election.getTitre());
            ps.setString(2, election.getTypeElection().name());
            ps.setString(3, election.getCibleType().name());
            ps.setString(4, election.getCibleValeur());
            ps.setTimestamp(5, Timestamp.valueOf(election.getDateDebut()));
            ps.setTimestamp(6, Timestamp.valueOf(election.getDateFin()));
            ps.setString(7, election.getStatut().name());
            ps.setInt(8, election.getCreatedBy());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'élection", e);
        }
    }

    @Override
    public void modifier(Election election) {
        String sql = "UPDATE elections SET titre=?, type_election=?, cible_type=?, cible_valeur=?, " +
                "date_debut=?, date_fin=?, statut=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, election.getTitre());
            ps.setString(2, election.getTypeElection().name());
            ps.setString(3, election.getCibleType().name());
            ps.setString(4, election.getCibleValeur());
            ps.setTimestamp(5, Timestamp.valueOf(election.getDateDebut()));
            ps.setTimestamp(6, Timestamp.valueOf(election.getDateFin()));
            ps.setString(7, election.getStatut().name());
            ps.setInt(8, election.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'élection", e);
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM elections WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'élection", e);
        }
    }

    @Override
    public Optional<Election> trouverParId(int id) {
        String sql = "SELECT * FROM elections WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToElection(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'élection", e);
        }
    }

    @Override
    public List<Election> trouverToutes() {
        String sql = "SELECT * FROM elections ORDER BY date_debut DESC";
        List<Election> elections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                elections.add(mapResultSetToElection(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des élections", e);
        }
        return elections;
    }

    @Override
    public List<Election> trouverParStatut(Election.Statut statut) {
        String sql = "SELECT * FROM elections WHERE statut = ? ORDER BY date_debut DESC";
        List<Election> elections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statut.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    elections.add(mapResultSetToElection(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par statut", e);
        }
        return elections;
    }

    @Override
    public void changerStatut(int id, Election.Statut nouveauStatut) {
        String sql = "UPDATE elections SET statut = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nouveauStatut.name());
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de statut", e);
        }
    }

    private Election mapResultSetToElection(ResultSet rs) throws SQLException {
        Election election = new Election();
        election.setId(rs.getInt("id"));
        election.setTitre(rs.getString("titre"));
        election.setTypeElection(Election.TypeElection.valueOf(rs.getString("type_election")));
        election.setCibleType(Election.CibleType.valueOf(rs.getString("cible_type")));
        election.setCibleValeur(rs.getString("cible_valeur"));
        election.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
        election.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
        election.setStatut(Election.Statut.valueOf(rs.getString("statut")));
        election.setCreatedBy(rs.getInt("created_by"));
        return election;
    }
}