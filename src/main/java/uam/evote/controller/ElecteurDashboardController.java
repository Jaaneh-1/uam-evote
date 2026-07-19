package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.VoteService;
import uam.evote.view.ElectionEligibleTableModel;

import java.util.List;
import java.util.stream.Collectors;

public class ElecteurDashboardController {

    @FXML
    private Label bienvenueLabel;

    @FXML
    private TableView<ElectionEligibleTableModel> tableElections;
    @FXML
    private TableColumn<ElectionEligibleTableModel, String> colTitre;
    @FXML
    private TableColumn<ElectionEligibleTableModel, String> colType;
    @FXML
    private TableColumn<ElectionEligibleTableModel, String> colDateFin;
    @FXML
    private TableColumn<ElectionEligibleTableModel, String> colStatutVote;

    private final VoteService voteService = new VoteService();
    private User utilisateurConnecte;

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatutVote.setCellValueFactory(new PropertyValueFactory<>("statutVote"));
    }

    public void initData(User user) {
        this.utilisateurConnecte = user;
        bienvenueLabel.setText("Bienvenue, " + user.getNomComplet() + " (" + user.getRole() + ")");
        chargerElections();
    }

    private void chargerElections() {
        List<Election> elections = voteService.listerElectionsEligibles(utilisateurConnecte);

        ObservableList<ElectionEligibleTableModel> data = FXCollections.observableArrayList(
                elections.stream()
                        .map(election -> new ElectionEligibleTableModel(
                                election,
                                voteService.aDejaVote(election.getId(), utilisateurConnecte.getId())
                        ))
                        .collect(Collectors.toList())
        );
        tableElections.setItems(data);
    }

    @FXML
    private void handleActualiser() {
        chargerElections();
    }

    @FXML
    private void handleVoter() {
        ElectionEligibleTableModel selection = tableElections.getSelectionModel().getSelectedItem();

        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection.");
            return;
        }

        if (voteService.aDejaVote(selection.getElection().getId(), utilisateurConnecte.getId())) {
            afficherMessage("Vous avez déjà voté à cette élection.");
            return;
        }

        ouvrirFormulaireVote(selection.getElection());
    }

    private void ouvrirFormulaireVote(Election election) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/vote-form.fxml")
            );
            javafx.scene.Parent root = loader.load();

            VoteFormController controller = loader.getController();
            controller.setOnSuccess(this::chargerElections);
            controller.initData(election, utilisateurConnecte);

            Stage popup = new Stage();
            popup.setTitle("Voter - " + election.getTitre());
            popup.setScene(new javafx.scene.Scene(root, 500, 500));
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de l'ouverture du formulaire de vote.");
            e.printStackTrace();
        }
    }

    private void afficherMessage(String texte) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, texte);
        alert.showAndWait();
    }
}