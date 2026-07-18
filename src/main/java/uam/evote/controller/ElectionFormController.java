package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.ElectionService;

import java.time.LocalDateTime;

public class ElectionFormController {

    @FXML
    private Label titreLabel;
    @FXML
    private TextField titreField;
    @FXML
    private ComboBox<Election.TypeElection> typeCombo;
    @FXML
    private ComboBox<Election.CibleType> cibleTypeCombo;
    @FXML
    private TextField cibleValeurField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private Label messageErreur;

    private final ElectionService electionService = new ElectionService();

    private Election electionEnEdition;
    private Runnable onSuccess;
    private int adminId; // id de l'admin connecté, pour created_by

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList(Election.TypeElection.values()));
        cibleTypeCombo.setItems(FXCollections.observableArrayList(Election.CibleType.values()));
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setElectionEnEdition(Election election) {
        this.electionEnEdition = election;
        titreLabel.setText("Modifier l'élection");

        titreField.setText(election.getTitre());
        typeCombo.setValue(election.getTypeElection());
        cibleTypeCombo.setValue(election.getCibleType());
        cibleValeurField.setText(election.getCibleValeur());
        dateDebutPicker.setValue(election.getDateDebut().toLocalDate());
        dateFinPicker.setValue(election.getDateFin().toLocalDate());
    }

    @FXML
    private void handleEnregistrer() {
        if (!validerChamps()) {
            return;
        }

        try {
            Election election = construireElectionDepuisFormulaire();

            if (electionEnEdition == null) {
                election.setCreatedBy(adminId);
                electionService.creerElection(election);
            } else {
                election.setId(electionEnEdition.getId());
                election.setStatut(electionEnEdition.getStatut());
                election.setCreatedBy(electionEnEdition.getCreatedBy());
                electionService.modifierElection(election);
            }

            if (onSuccess != null) {
                onSuccess.run();
            }
            fermerFenetre();

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        }
    }

    private Election construireElectionDepuisFormulaire() {
        Election election = new Election();
        election.setTitre(titreField.getText().trim());
        election.setTypeElection(typeCombo.getValue());
        election.setCibleType(cibleTypeCombo.getValue());
        election.setCibleValeur(cibleValeurField.getText().trim());
        election.setDateDebut(dateDebutPicker.getValue().atStartOfDay());
        election.setDateFin(dateFinPicker.getValue().atTime(23, 59));
        return election;
    }

    private boolean validerChamps() {
        if (titreField.getText().isBlank() || typeCombo.getValue() == null
                || cibleTypeCombo.getValue() == null || cibleValeurField.getText().isBlank()
                || dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            afficherErreur("Veuillez remplir tous les champs.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }
}