package uam.evote.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;

    // Bloc statique : exécuté une seule fois, au premier chargement de la classe.
    // Il lit le fichier db.properties pour récupérer les identifiants de connexion.
    static {
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                        "Fichier db.properties introuvable dans src/main/resources. " +
                                "Copiez db.properties.example vers db.properties et renseignez votre mot de passe."
                );
            }

            Properties props = new Properties();
            props.load(input);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture de db.properties", e);
        }
    }

    // Empêche d'instancier cette classe (elle ne sert qu'à fournir des connexions)
    private DatabaseConnection() {
    }

    /**
     * Ouvre et retourne une nouvelle connexion à la base de données.
     * À utiliser dans un try-with-resources pour qu'elle se ferme automatiquement.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Petit test manuel : vérifie que la connexion à la base fonctionne.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connexion réussie à la base : " + conn.getCatalog());
        } catch (SQLException e) {
            System.out.println("Échec de connexion : " + e.getMessage());
        }
    }
}