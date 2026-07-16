package uam.evote.dao;

import uam.evote.model.User;
import uam.evote.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public int ajouter(User user) {
        String sql = "INSERT INTO users (matricule, nom, prenom, email, login, password, role, classe, departement, ufr) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getMatricule());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getLogin());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getRole().name());
            ps.setString(8, user.getClasse());
            ps.setString(9, user.getDepartement());
            ps.setString(10, user.getUfr());

            ps.executeUpdate();

            // Récupère l'id auto-généré par MySQL pour le renvoyer
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur", e);
        }
    }

    @Override
    public void modifier(User user) {
        String sql = "UPDATE users SET matricule=?, nom=?, prenom=?, email=?, login=?, " +
                "role=?, classe=?, departement=?, ufr=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getMatricule());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getLogin());
            ps.setString(6, user.getRole().name());
            ps.setString(7, user.getClasse());
            ps.setString(8, user.getDepartement());
            ps.setString(9, user.getUfr());
            ps.setInt(10, user.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'utilisateur", e);
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    @Override
    public Optional<User> trouverParId(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur", e);
        }
    }

    @Override
    public Optional<User> trouverParLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par login", e);
        }
    }

    @Override
    public List<User> trouverTous() {
        String sql = "SELECT * FROM users ORDER BY nom, prenom";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
        }
        return users;
    }

    @Override
    public List<User> rechercher(String motCle) {
        String sql = "SELECT * FROM users WHERE nom LIKE ? OR prenom LIKE ? OR matricule LIKE ?";
        List<User> users = new ArrayList<>();
        String pattern = "%" + motCle + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche d'utilisateurs", e);
        }
        return users;
    }

    // Convertit une ligne de résultat SQL en objet User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setMatricule(rs.getString("matricule"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setClasse(rs.getString("classe"));
        user.setDepartement(rs.getString("departement"));
        user.setUfr(rs.getString("ufr"));
        return user;
    }
}