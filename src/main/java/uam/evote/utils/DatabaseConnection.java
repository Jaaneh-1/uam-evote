package uam.evote.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String port;
    private static String dbName;
    private static String user;
    private static String password;

    private static String host = "";

    // Bloc statique : lit une seule fois, au démarrage, les réglages qui ne changent
    // jamais d'un poste à l'autre (port, nom de la base, identifiants MySQL).
    // L'adresse du serveur, elle, N'EST PLUS ici : elle est saisie par l'utilisateur
    // sur l'écran de connexion (voir setHost), pour permettre le même installeur
    // sur plusieurs postes du réseau sans jamais recompiler.
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

            port = props.getProperty("db.port", "3306");
            dbName = props.getProperty("db.name", "uam_evote");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture de db.properties", e);
        }
    }

    private DatabaseConnection() {
    }

    /**
     * Définit l'adresse (IP ou nom d'hôte) du serveur MySQL à utiliser pour toutes
     * les connexions suivantes de cette session.
     */
    public static void setHost(String nouvelHote) {
        host = nouvelHote;
    }

    public static String getHost() {
        return host;
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        setHost("localhost");
        try (Connection conn = getConnection()) {
            System.out.println("Connexion réussie à la base : " + conn.getCatalog());
        } catch (SQLException e) {
            System.out.println("Échec de connexion : " + e.getMessage());
        }
    }
}