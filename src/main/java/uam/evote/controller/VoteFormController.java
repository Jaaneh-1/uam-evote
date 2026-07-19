package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uam.evote.model.Candidat;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.VoteService;

import java.util.List;

public class VoteFormController {

    @FXML
    private Label titreElectionLabel;
    @FXML
    private VBox candidatsContainer;
    @FXML
    private Label messageErreur;

    private final VoteService voteService = new VoteService();
    private final ToggleGroup groupeCandidats = new ToggleGroup();

    private Election election;
    private User electeur;
    private Runnable onSuccess;

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
            candidatsContainer.getChildren().add(creerBlocCandidat(candidat));
        }
    }

    /**
     * Construit dynamiquement un bloc visuel pour un candidat : bouton radio + infos.
     * On fait ça en Java (pas en FXML) car le nombre de candidats varie selon l'élection.
     */
    private VBox creerBlocCandidat(Candidat candidat) {
        RadioButton radio = new RadioButton(candidat.getNomComplet());
        radio.setToggleGroup(groupeCandidats);
        radio.setUserData(candidat.getId()); // permet de retrouver l'id du candidat sélectionné
        radio.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label profession = new Label(
                candidat.getProfession() != null ? candidat.getProfession() : ""
        );
        profession.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label programme = new Label(
                candidat.getProgramme() != null ? candidat.getProgramme() : ""
        );
        programme.setWrapText(true);
        programme.setStyle("-fx-font-size: 12px;");

        VBox bloc = new VBox(4, radio, profession, programme);
        bloc.setPadding(new Insets(10));
        bloc.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #ddd; -fx-border-radius: 6;");

        return bloc;
    }

    @FXML
    private void handleConfirmer() {
        Toggle selection = groupeCandidats.getSelectedToggle();

        if (selection == null) {
            afficherErreur("Veuillez sélectionner un candidat avant de confirmer.");
            return;
        }

        int candidatId = (int) selection.getUserData();

        // Confirmation explicite avant validation définitive, puisqu'un vote
        // ne peut jamais être modifié après coup (contrainte du sujet)
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmez-vous votre vote ? Ce choix est définitif et ne pourra pas être modifié.");
        confirmation.setHeaderText(null);

        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                enregistrerVote(candidatId);
            }
        });
    }

    private void enregistrerVote(int candidatId) {
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