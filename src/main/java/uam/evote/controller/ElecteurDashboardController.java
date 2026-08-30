package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.VoteService;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ElecteurDashboardController {

    @FXML
    private Label avatarLabel;
    @FXML
    private ImageView avatarImage;
    @FXML
    private Label nomLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label titreAccueilLabel;

    @FXML
    private VBox carteVedette;
    @FXML
    private Label vedetteTitreLabel;
    @FXML
    private Label vedetteSousLabel;
    @FXML
    private Button vedetteBouton;

    @FXML
    private Label statOuvertesLabel;
    @FXML
    private Label statVotesLabel;
    @FXML
    private Label statEnAttenteLabel;

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private FlowPane cardsContainerTerminees;

    private final VoteService voteService = new VoteService();
    private User utilisateurConnecte;

    public void initData(User user) {
        this.utilisateurConnecte = user;

        titreAccueilLabel.setText("Bienvenue, " + user.getPrenom());
        nomLabel.setText(user.getNomComplet());
        roleLabel.setText(user.getRole().name());

        configurerAvatar(user);
        chargerElections();

        configurerAvatar(user);
        chargerElections();
        chargerElectionsTerminees();

    }

    private void configurerAvatar(User user) {
        if (user.getPhoto() != null && !user.getPhoto().isBlank() && new File(user.getPhoto()).exists()) {
            Image image = new Image(new File(user.getPhoto()).toURI().toString());
            avatarImage.setImage(image);

            Circle clip = new Circle(28, 28, 28);
            avatarImage.setClip(clip);
            avatarLabel.setVisible(false);
        } else {
            String initiales = "" + user.getPrenom().charAt(0) + user.getNom().charAt(0);
            avatarLabel.setText(initiales.toUpperCase());
            avatarImage.setVisible(false);
        }
    }

    private void chargerElections() {
        cardsContainer.getChildren().clear();

        List<Election> elections = voteService.listerElectionsEligibles(utilisateurConnecte);

        int nbVotes = 0;
        Election prochaineAVoter = null;

        for (Election election : elections) {
            boolean dejaVote = voteService.aDejaVote(election.getId(), utilisateurConnecte.getId());
            if (dejaVote) {
                nbVotes++;
            } else if (prochaineAVoter == null || election.getDateFin().isBefore(prochaineAVoter.getDateFin())) {
                prochaineAVoter = election;
            }
            cardsContainer.getChildren().add(creerCarteElection(election, dejaVote));
        }

        // Statistiques
        statOuvertesLabel.setText(String.valueOf(elections.size()));
        statVotesLabel.setText(String.valueOf(nbVotes));
        statEnAttenteLabel.setText(String.valueOf(elections.size() - nbVotes));

        // Carte vedette
        configurerCarteVedette(prochaineAVoter);

        if (elections.isEmpty()) {
            Label vide = new Label("Aucune élection ouverte pour le moment.");
            vide.setStyle("-fx-text-fill: #A9776A; -fx-font-size: 13px;");
            cardsContainer.getChildren().add(vide);
        }
    }

    private void configurerCarteVedette(Election election) {
        if (election == null) {
            carteVedette.setVisible(false);
            carteVedette.setManaged(false);
            return;
        }

        carteVedette.setVisible(true);
        carteVedette.setManaged(true);

        vedetteTitreLabel.setText(election.getTitre());
        vedetteSousLabel.setText("Clôture le "
                + election.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));

        Election finalElection = election;
        vedetteBouton.setOnAction(e -> ouvrirFormulaireVote(finalElection));
    }

    private VBox creerCarteElection(Election election, boolean dejaVote) {
        Label titre = new Label(election.getTitre());
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7A3E2E;");
        titre.setWrapText(true);

        Label type = new Label(election.getTypeElection().name());
        type.setStyle("-fx-font-size: 11px; -fx-text-fill: #A9776A;");

        Label dateFin = new Label("Clôture : "
                + election.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
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

    private void chargerElectionsTerminees() {
        cardsContainerTerminees.getChildren().clear();

        List<Election> electionsTerminees = voteService.listerElectionsTermineesEligibles(utilisateurConnecte);

        if (electionsTerminees.isEmpty()) {
            Label vide = new Label("Aucune élection terminée pour le moment.");
            vide.setStyle("-fx-text-fill: #A9776A; -fx-font-size: 13px;");
            cardsContainerTerminees.getChildren().add(vide);
            return;
        }

        for (Election election : electionsTerminees) {
            cardsContainerTerminees.getChildren().add(creerCarteElectionTerminee(election));
        }
    }

    private VBox creerCarteElectionTerminee(Election election) {
        Label titre = new Label(election.getTitre());
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7A3E2E;");
        titre.setWrapText(true);

        Label type = new Label(election.getTypeElection().name());
        type.setStyle("-fx-font-size: 11px; -fx-text-fill: #A9776A;");

        Label cloture = new Label("Clôturée le "
                + election.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        cloture.setStyle("-fx-font-size: 11px; -fx-text-fill: #A9776A;");

        Button boutonResultats = new Button("Voir résultats");
        boutonResultats.setMaxWidth(Double.MAX_VALUE);
        boutonResultats.getStyleClass().add("bouton-principal");
        boutonResultats.setOnAction(e -> ouvrirResultats(election));

        Region spacer = new Region();

        VBox carte = new VBox(8, titre, type, cloture, spacer, boutonResultats);
        carte.getStyleClass().add("election-card");
        carte.setPrefWidth(260);

        return carte;
    }

    private void ouvrirResultats(Election election) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resultats.fxml"));
            Parent root = loader.load();

            ResultatsController controller = loader.getController();
            controller.initData(election);

            Stage popup = new Stage();
            popup.setTitle("Résultats - " + election.getTitre());
            Scene scene = new Scene(root, 820, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            popup.setScene(scene);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherErreurChargement();
        }
    }

    @FXML
    private void handleActualiser() {
        chargerElections();
        chargerElectionsTerminees();
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
            Scene scene = new Scene(root, 500, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            popup.setScene(scene);
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