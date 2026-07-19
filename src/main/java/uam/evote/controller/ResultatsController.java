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

    public void initData(Election election) {
        titreLabel.setText("Résultats - " + election.getTitre());

        List<VoteService.ResultatCandidat> resultats = voteService.calculerResultats(election.getId());

        int nbElecteurs = voteService.compterElecteursEligibles(election, userService.listerTous());
        int nbVotes = resultats.stream().mapToInt(VoteService.ResultatCandidat::getNbVotes).sum();
        double taux = nbElecteurs == 0 ? 0 : (nbVotes * 100.0 / nbElecteurs);

        electeursLabel.setText("Électeurs éligibles : " + nbElecteurs);
        votesLabel.setText("Votes exprimés : " + nbVotes);
        participationLabel.setText(String.format("Participation : %.1f %%", taux));

        afficherVainqueur(resultats, nbVotes);
        remplirPieChart(resultats);
        remplirBarChart(resultats);
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
}