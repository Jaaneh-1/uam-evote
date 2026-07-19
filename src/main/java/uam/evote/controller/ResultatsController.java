package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import uam.evote.model.Election;
import uam.evote.service.UserService;
import uam.evote.service.VoteService;

import java.util.List;
import javafx.stage.FileChooser;
import uam.evote.service.ExportService;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResultatsController {

    @FXML
    private Label titreLabel;
    @FXML
    private Label electeursLabel;
    @FXML
    private Label votesLabel;
    @FXML
    private Label participationLabel;
    @FXML
    private Label vainqueurLabel;
    @FXML
    private PieChart pieChart;
    @FXML
    private BarChart<String, Number> barChart;

    private final VoteService voteService = new VoteService();
    private final UserService userService = new UserService();

    private final ExportService exportService = new ExportService();

    private Election electionCourante;
    private List<VoteService.ResultatCandidat> resultatsCourants;
    private int nbElecteursCourant;

    public void initData(Election election) {
        this.electionCourante = election;
        titreLabel.setText("Résultats - " + election.getTitre());

        this.resultatsCourants = voteService.calculerResultats(election.getId());
        this.nbElecteursCourant = voteService.compterElecteursEligibles(election, userService.listerTous());

        int nbVotes = resultatsCourants.stream().mapToInt(VoteService.ResultatCandidat::getNbVotes).sum();
        double taux = nbElecteursCourant == 0 ? 0 : (nbVotes * 100.0 / nbElecteursCourant);

        electeursLabel.setText("Électeurs éligibles : " + nbElecteursCourant);
        votesLabel.setText("Votes exprimés : " + nbVotes);
        participationLabel.setText(String.format("Participation : %.1f %%", taux));

        afficherVainqueur(resultatsCourants, nbVotes);
        remplirPieChart(resultatsCourants);
        remplirBarChart(resultatsCourants);
    }

    private void afficherVainqueur(List<VoteService.ResultatCandidat> resultats, int nbVotes) {
        if (resultats.isEmpty() || nbVotes == 0) {
            vainqueurLabel.setText("Aucun vote n'a encore été exprimé.");
            return;
        }

        VoteService.ResultatCandidat premier = resultats.get(0);

        // Vérifie une éventuelle égalité en tête (cas rare mais à gérer proprement)
        boolean exAequo = resultats.size() > 1
                && resultats.get(1).getNbVotes() == premier.getNbVotes();

        if (exAequo) {
            vainqueurLabel.setText("Égalité en tête, aucun vainqueur désigné automatiquement.");
        } else {
            vainqueurLabel.setText(
                    "Vainqueur : " + premier.getCandidat().getNomComplet()
                            + " (" + premier.getNbVotes() + " voix)"
            );
        }
    }

    private void remplirPieChart(List<VoteService.ResultatCandidat> resultats) {
        var data = FXCollections.<PieChart.Data>observableArrayList();
        for (VoteService.ResultatCandidat resultat : resultats) {
            data.add(new PieChart.Data(
                    resultat.getCandidat().getNomComplet() + " (" + resultat.getNbVotes() + ")",
                    resultat.getNbVotes()
            ));
        }
        pieChart.setData(data);
        pieChart.setTitle("Répartition des votes");
    }

    private void remplirBarChart(List<VoteService.ResultatCandidat> resultats) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Votes");

        for (VoteService.ResultatCandidat resultat : resultats) {
            serie.getData().add(new XYChart.Data<>(
                    resultat.getCandidat().getNomComplet(), resultat.getNbVotes()
            ));
        }

        barChart.getData().add(serie);
        barChart.setTitle("Votes par candidat");
        barChart.setLegendVisible(false);
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les résultats en PDF");
        fileChooser.setInitialFileName("resultats_" + electionCourante.getId() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        File fichier = fileChooser.showSaveDialog(titreLabel.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        try {
            exportService.exporterPDF(electionCourante, resultatsCourants, nbElecteursCourant,
                    fichier.getAbsolutePath());
            afficherInfo("Export PDF réussi : " + fichier.getName());
        } catch (IOException e) {
            afficherInfo("Erreur lors de l'export PDF : " + e.getMessage());
        }
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les résultats en CSV");
        fileChooser.setInitialFileName("resultats_" + electionCourante.getId() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File fichier = fileChooser.showSaveDialog(titreLabel.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        try {
            exportService.exporterCSV(electionCourante, resultatsCourants, fichier.getAbsolutePath());
            afficherInfo("Export CSV réussi : " + fichier.getName());
        } catch (IOException e) {
            afficherInfo("Erreur lors de l'export CSV : " + e.getMessage());
        }
    }

    private void afficherInfo(String texte) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION, texte
        );
        alert.showAndWait();
    }
}