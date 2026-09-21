package uam.evote.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Dossier de données de l'application, indépendant du dossier de lancement
 * (contrairement à un chemin relatif "data/...", qui ne fonctionne que si on lance
 * l'appli depuis le dossier du projet — pas depuis un .exe installé). Toutes les
 * données générées ou uploadées par l'application (photos, cache, file d'attente
 * hors ligne) vivent ici, dans le dossier personnel de l'utilisateur Windows,
 * toujours accessible en écriture, quel que soit l'endroit où l'appli est installée.
 */
public class AppDataDir {

    private static final Path RACINE = Paths.get(System.getProperty("user.home"), ".uam-evote");

    public static Path get(String... segments) {
        Path chemin = RACINE;
        for (String segment : segments) {
            chemin = chemin.resolve(segment);
        }
        return chemin;
    }
}