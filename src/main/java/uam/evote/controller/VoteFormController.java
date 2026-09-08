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
    @FXML
    private VBox rootBox;

    private final VoteService voteService = new VoteService();

    private Election election;
    private User electeur;
    private Runnable onSuccess;

    private final List<VBox> cartesCandidats = new ArrayList<>();
    private Integer candidatSelectionneId = null;

    private final java.util.Map<Integer, Candidat> candidatsParId = new java.util.HashMap<>();

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
            candidatsParId.put(candidat.getId(), candidat);
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
            afficherConfirmation(candidatsParId.get(candidatId), false);

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

        afficherConfirmation(candidatsParId.get(candidatId), true);
    }

    private void afficherConfirmation(Candidat candidat, boolean horsLigne) {
        Label icone = new Label(horsLigne ? "⏳" : "✓");
        icone.getStyleClass().add(horsLigne ? "icone-confirmation-attente" : "icone-confirmation-succes");
        icone.setAlignment(javafx.geometry.Pos.CENTER);

        Label titre = new Label(horsLigne ? "Vote enregistré localement" : "Vote enregistré !");
        titre.getStyleClass().add("titre");

        String texteCandidat = candidat != null ? candidat.getNomComplet() : "le candidat sélectionné";
        Label recap = new Label("Vous avez voté pour " + texteCandidat + "\nà l'élection " + election.getTitre() + ".");
        recap.getStyleClass().add("sous-titre");
        recap.setWrapText(true);
        recap.setStyle("-fx-text-alignment: center;");
        recap.setAlignment(javafx.geometry.Pos.CENTER);

        Label info = new Label(horsLigne
                ? "Connexion au serveur indisponible : votre vote sera transmis automatiquement dès le retour de la connexion."
                : "Votre choix a bien été pris en compte. Merci d'avoir participé au scrutin.");
        info.setWrapText(true);
        info.setStyle("-fx-text-fill: #A9776A; -fx-font-size: 12px; -fx-text-alignment: center;");
        info.setAlignment(javafx.geometry.Pos.CENTER);

        Button boutonFermer = new Button("Fermer");
        boutonFermer.getStyleClass().add("bouton-principal");
        boutonFermer.setOnAction(e -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
            fermerFenetre();
        });

        VBox panneau = new VBox(14, icone, titre, recap, info, boutonFermer);
        panneau.setAlignment(javafx.geometry.Pos.CENTER);
        panneau.setPadding(new Insets(30, 20, 10, 20));

        rootBox.getChildren().setAll(panneau);
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) rootBox.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }
}