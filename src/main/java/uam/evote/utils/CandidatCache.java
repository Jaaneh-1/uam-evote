package uam.evote.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import uam.evote.model.Candidat;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cache local de la liste des candidats par élection, mis à jour à chaque
 * chargement réussi depuis le serveur. Permet d'ouvrir l'écran de vote
 * et de voter en mode hors ligne même quand la base est injoignable.
 */
public class CandidatCache {

    private static final Path FICHIER = Paths.get("data", "candidats_cache.csv");

    public void sauvegarder(int electionId, List<Candidat> candidats) {
        try {
            Files.createDirectories(FICHIER.getParent());

            List<Candidat> autres = listerTout().stream()
                    .filter(c -> c.getElectionId() != electionId)
                    .collect(Collectors.toList());
            autres.addAll(candidats);

            try (CSVWriter writer = new CSVWriter(new FileWriter(FICHIER.toFile(), false))) {
                writer.writeNext(new String[]{"id", "electionId", "nom", "prenom", "photo", "profession", "programme"});
                for (Candidat c : autres) {
                    writer.writeNext(new String[]{
                            String.valueOf(c.getId()),
                            String.valueOf(c.getElectionId()),
                            c.getNom(),
                            c.getPrenom(),
                            c.getPhoto() == null ? "" : c.getPhoto(),
                            c.getProfession() == null ? "" : c.getProfession(),
                            c.getProgramme() == null ? "" : c.getProgramme()
                    });
                }
            }
        } catch (IOException e) {
            // Le cache est une aide, pas une exigence : on ne bloque jamais l'appli s'il échoue à s'écrire.
        }
    }

    public List<Candidat> listerParElection(int electionId) {
        return listerTout().stream()
                .filter(c -> c.getElectionId() == electionId)
                .collect(Collectors.toList());
    }

    private List<Candidat> listerTout() {
        List<Candidat> resultat = new ArrayList<>();
        if (!Files.exists(FICHIER)) {
            return resultat;
        }
        try (CSVReader reader = new CSVReader(new FileReader(FICHIER.toFile()))) {
            List<String[]> lignes = reader.readAll();
            for (int i = 1; i < lignes.size(); i++) {
                String[] l = lignes.get(i);
                Candidat c = new Candidat();
                c.setId(Integer.parseInt(l[0]));
                c.setElectionId(Integer.parseInt(l[1]));
                c.setNom(l[2]);
                c.setPrenom(l[3]);
                c.setPhoto(l[4].isBlank() ? null : l[4]);
                c.setProfession(l[5].isBlank() ? null : l[5]);
                c.setProgramme(l[6].isBlank() ? null : l[6]);
                resultat.add(c);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return resultat;
    }
}