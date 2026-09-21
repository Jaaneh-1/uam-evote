package uam.evote.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Mémorise localement, sur le poste de l'utilisateur (pas dans le programme lui-même),
 * la dernière adresse de serveur saisie sur l'écran de connexion. Permet à un
 * administrateur de communiquer une nouvelle adresse par simple message : l'utilisateur
 * la saisit une fois, elle est ensuite mémorisée automatiquement pour les prochaines
 * connexions, sans jamais avoir à modifier de fichier ni réinstaller l'application.
 */
public class ServerConfig {

    private static final Path FICHIER = Paths.get(
            System.getProperty("user.home"), ".uam-evote", "config.properties"
    );

    public static String lireAdresseServeur() {
        if (!Files.exists(FICHIER)) {
            return "";
        }
        try (InputStream input = Files.newInputStream(FICHIER)) {
            Properties props = new Properties();
            props.load(input);
            return props.getProperty("server.host", "");
        } catch (IOException e) {
            return "";
        }
    }

    public static void sauvegarderAdresseServeur(String adresse) {
        try {
            Files.createDirectories(FICHIER.getParent());
            Properties props = new Properties();
            props.setProperty("server.host", adresse);
            try (OutputStream output = Files.newOutputStream(FICHIER)) {
                props.store(output, "Configuration UAM e-Vote - ne pas modifier a la main");
            }
        } catch (IOException e) {
            // Non bloquant : au pire, l'adresse ne sera pas mémorisée pour la prochaine fois.
        }
    }
}