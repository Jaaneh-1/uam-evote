package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import uam.evote.model.Candidat;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.ConnectivityService;
import uam.evote.service.VoteService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.FlowPane;

public class VoteFormController {

    @FXML
    private Label titreElectionLabel;
    @FXML
    private FlowPane candidatsContainer;
    @FXML
    private Label messageErreur;

    private final VoteService voteService = new VoteService();

    private Election election;
    private User electeur;
    private Runnable onSuccess;

    private final List<VBox> cartesCandidats = new ArrayList<>();
    private Integer candidatSelectionneId = null;

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void initData(Election election, User electeur) {
        this.election = election;
        this.electeur = electeur;

        titreElectionLabel.setText(election.getTitre());
        chargerCandidats();
    }



    private void chargerCandidats() {
        List<Candidat> candidats = voteService.listerCandidats(election.getId());

        for (Candidat candidat : candidats) {
            VBox carte = creerCarteCandidat(candidat);
            cartesCandidats.add(carte);
            candidatsContainer.getChildren().add(carte);
        }
    }

    private VBox creerCarteCandidat(Candidat candidat) {
        ImageView photo = new ImageView();
        photo.setFitWidth(70);
        photo.setFitHeight(70);
        photo.getStyleClass().add("candidat-photo");

        if (candidat.getPhoto() != null && new File(candidat.getPhoto()).exists()) {
            photo.setImage(new Image(new File(candidat.getPhoto()).toURI().toString()));
        }
        Circle clip = new Circle(35, 35, 35);
        photo.setClip(clip);

        Label nom = new Label(candidat.getNomComplet());
        nom.getStyleClass().add("candidat-nom-grid");
        nom.setMaxWidth(130);

        Label profession = new Label(
                candidat.getProfession() != null ? candidat.getProfession() : ""
        );
        profession.getStyleClass().add("candidat-profession-grid");

        Button boutonSelection = new Button("Sélectionner");
        boutonSelection.getStyleClass().add("bouton-selection");

        VBox carte = new VBox(8, photo, nom, profession, boutonSelection);
        carte.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        carte.getStyleClass().add("candidat-card-grid");
        carte.setUserData(candidat.getId());

        Runnable action = () -> selectionnerCarte(carte, boutonSelection, candidat.getId());
        carte.setOnMouseClicked(e -> action.run());
        boutonSelection.setOnAction(e -> action.run());

        return carte;
    }

    private final java.util.Map<VBox, Button> boutonsParCarte = new java.util.HashMap<>();

    private void selectionnerCarte(VBox carteChoisie, Button boutonChoisi, int candidatId) {
        for (VBox carte : cartesCandidats) {
            carte.getStyleClass().remove("candidat-card-grid-selected");
        }
        for (Button b : boutonsParCarte.values()) {
            b.getStyleClass().remove("bouton-selection-actif");
            b.setText("Sélectionner");
        }

        carteChoisie.getStyleClass().add("candidat-card-grid-selected");
        boutonChoisi.getStyleClass().add("bouton-selection-actif");
        boutonChoisi.setText("✓ Sélectionné");
        boutonsParCarte.put(carteChoisie, boutonChoisi);

        candidatSelectionneId = candidatId;
    }



    @FXML
    private void handleConfirmer() {
        if (candidatSelectionneId == null) {
            afficherErreur("Veuillez sélectionner un candidat avant de confirmer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmez-vous votre vote ? Ce choix est définitif et ne pourra pas être modifié.");
        confirmation.setHeaderText(null);

        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                enregistrerVote(candidatSelectionneId);
            }
        });
    }

    private void enregistrerVote(int candidatId) {
        ConnectivityService connectivityService = new ConnectivityService();

        if (!connectivityService.estConnecte()) {
            enregistrerVoteHorsLigne(candidatId);
            return;
        }

        try {
            voteService.voter(electeur, election.getId(), candidatId);

            Alert succes = new Alert(Alert.AlertType.INFORMATION, "Votre vote a bien été enregistré. Merci !");
            succes.setHeaderText(null);
            succes.showAndWait();

            if (onSuccess != null) {
                onSuccess.run();
            }
            fermerFenetre();

        } catch (IllegalStateException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void enregistrerVoteHorsLigne(int candidatId) {
        uam.evote.utils.OfflineVoteQueue queue = new uam.evote.utils.OfflineVoteQueue();

        if (queue.dejaEnAttente(election.getId(), electeur.getId())) {
            afficherErreur("Vous avez déjà un vote en attente de synchronisation pour cette élection.");
            return;
        }

        queue.ajouter(election.getId(), candidatId, electeur.getId());

        Alert info = new Alert(Alert.AlertType.INFORMATION,
                "Connexion au serveur indisponible. Votre vote a été enregistré localement "
                        + "et sera transmis automatiquement dès le retour de la connexion.");
        info.setHeaderText(null);
        info.showAndWait();

        if (onSuccess != null) {
            onSuccess.run();
        }
        fermerFenetre();
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) titreElectionLabel.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }
}