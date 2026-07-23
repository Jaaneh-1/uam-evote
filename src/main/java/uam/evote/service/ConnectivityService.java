package uam.evote.service;

import uam.evote.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectivityService {

    /**
     * Vérifie si le serveur de base de données est actuellement joignable.
     * Utilisé avant toute opération critique (comme le vote) pour décider
     * si on doit basculer en mode hors ligne.
     */
    public boolean estConnecte() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}