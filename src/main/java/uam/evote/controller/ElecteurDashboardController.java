package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.VoteService;

import java.util.List;

public class ElecteurDashboardController {

    @FXML
    private Label avatarLabel;
    @FXML
    private Label nomLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private FlowPane cardsContainer;

    private final VoteService voteService = new VoteService();
    private User utilisateurConnecte;

    public void initData(User user) {
        this.utilisateurConnecte = user;

        String initiales = "" + user.getPrenom().charAt(0) + user.getNom().charAt(0);
        avatarLabel.setText(initiales.toUpperCase());
        nomLabel.setText(user.getNomComplet());
        roleLabel.setText(user.getRole().name());

        chargerElections();
    }

    private void chargerElections() {
        cardsContainer.getChildren().clear();

        List<Election> elections = voteService.listerElectionsEligibles(utilisateurConnecte);

        if (elections.isEmpty()) {
            Label vide = new Label("Aucune élection ouverte pour le moment.");
            vide.setStyle("-fx-text-fill: #A9776A; -fx-font-size: 13px;");
            cardsContainer.getChildren().add(vide);
            return;
        }

        for (Election election : elections) {
            boolean dejaVote = voteService.aDejaVote(election.getId(), utilisateurConnecte.getId());
            cardsContainer.getChildren().add(creerCarteElection(election, dejaVote));
        }
    }

    private VBox creerCarteElection(Election election, boolean dejaVote) {
        Label titre = new Label(election.getTitre());
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7A3E2E;");
        titre.setWrapText(true);

        Label type = new Label(election.getTypeElection().name());
        type.setStyle("-fx-font-size: 11px; -fx-text-fill: #A9776A;");

        Label dateFin = new Label("Clôture : "
                + election.getDateFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateFin.setStyle("-fx-font-size: 11px; -fx-text-fill: #A9776A;");

        Label badge = new Label(dejaVote ? "Déjà voté ✓" : "Vous pouvez voter");
        badge.getStyleClass().add(dejaVote ? "badge-succes" : "badge-attente");

        Button boutonVoter = new Button(dejaVote ? "Déjà voté" : "Voter");
        boutonVoter.setDisable(dejaVote);
        boutonVoter.setMaxWidth(Double.MAX_VALUE);
        boutonVoter.getStyleClass().add(dejaVote ? "button" : "bouton-principal");
        boutonVoter.setOnAction(e -> ouvrirFormulaireVote(election));

        Region spacer = new Region();

        VBox carte = new VBox(8, titre, type, dateFin, badge, spacer, boutonVoter);
        carte.getStyleClass().add("election-card");
        carte.setPrefWidth(260);

        return carte;
    }

    @FXML
    private void handleActualiser() {
        chargerElections();
    }

    private void ouvrirFormulaireVote(Election election) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vote-form.fxml"));
            Parent root = loader.load();

            VoteFormController controller = loader.getController();
            controller.setOnSuccess(this::chargerElections);
            controller.initData(election, utilisateurConnecte);

            Stage popup = new Stage();
            popup.setTitle("Voter - " + election.getTitre());
            popup.setScene(new Scene(root, 500, 500));
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherErreurChargement();
        } catch (RuntimeException e) {
            afficherErreurConnexion();
        }
    }

    @FXML
    private void handleDeconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            Scene scene = new Scene(root, 480, 560);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMinWidth(0);
            stage.setMinHeight(0);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherErreurChargement() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR, "Erreur lors de l'ouverture du formulaire de vote.");
        alert.showAndWait();
    }

    private void afficherErreurConnexion() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING,
                "Connexion au serveur impossible. Réessayez une fois la connexion rétablie.");
        alert.showAndWait();
    }
}