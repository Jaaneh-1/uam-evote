package uam.evote.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import uam.evote.model.VoteEnAttente;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * File d'attente locale des votes enregistrés alors que le serveur était injoignable.
 * Stockée dans un simple fichier CSV sur le disque de l'électeur/de la machine,
 * jamais transmise à un tiers avant la synchronisation officielle avec le serveur.
 */
public class OfflineVoteQueue {

    private static final Path FICHIER = Paths.get("data", "votes_en_attente.csv");

    public void ajouter(int electionId, int candidatId, int utilisateurId) {
        try {
            Files.createDirectories(FICHIER.getParent());

            boolean existeDeja = Files.exists(FICHIER);

            try (CSVWriter writer = new CSVWriter(new FileWriter(FICHIER.toFile(), true))) {
                if (!existeDeja) {
                    writer.writeNext(new String[]{"electionId", "candidatId", "utilisateurId", "dateVote"});
                }
                writer.writeNext(new String[]{
                        String.valueOf(electionId),
                        String.valueOf(candidatId),
                        String.valueOf(utilisateurId),
                        LocalDateTime.now().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'enregistrer le vote en mode hors ligne.", e);
        }
    }

    public boolean dejaEnAttente(int electionId, int utilisateurId) {
        return lister().stream().anyMatch(v ->
                v.getElectionId() == electionId && v.getUtilisateurId() == utilisateurId
        );
    }

    public List<VoteEnAttente> lister() {
        List<VoteEnAttente> resultat = new ArrayList<>();

        if (!Files.exists(FICHIER)) {
            return resultat;
        }

        try (CSVReader reader = new CSVReader(new FileReader(FICHIER.toFile()))) {
            List<String[]> lignes = reader.readAll();
            for (int i = 1; i < lignes.size(); i++) { // on saute l'en-tête
                String[] l = lignes.get(i);
                resultat.add(new VoteEnAttente(
                        Integer.parseInt(l[0]), Integer.parseInt(l[1]),
                        Integer.parseInt(l[2]), l[3]
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de lecture des votes en attente.", e);
        }
        return resultat;
    }

    public int compter() {
        return lister().size();
    }

    /**
     * Retire de la file les votes qui ont été traités (synchronisés ou définitivement rejetés),
     * en réécrivant le fichier avec seulement ceux qui restent à traiter.
     */
    public void retirer(List<VoteEnAttente> traites) {
        List<VoteEnAttente> restants = lister();
        restants.removeIf(v -> traites.stream().anyMatch(t ->
                t.getElectionId() == v.getElectionId()
                        && t.getUtilisateurId() == v.getUtilisateurId()
                        && t.getDateVote().equals(v.getDateVote())
        ));

        try {
            Files.createDirectories(FICHIER.getParent());
            try (CSVWriter writer = new CSVWriter(new FileWriter(FICHIER.toFile(), false))) {
                writer.writeNext(new String[]{"electionId", "candidatId", "utilisateurId", "dateVote"});
                for (VoteEnAttente v : restants) {
                    writer.writeNext(new String[]{
                            String.valueOf(v.getElectionId()), String.valueOf(v.getCandidatId()),
                            String.valueOf(v.getUtilisateurId()), v.getDateVote()
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la mise à jour des votes en attente.", e);
        }
    }
}