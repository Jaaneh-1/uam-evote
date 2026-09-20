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
    private ComboBox<String> cibleValeurCombo;
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

        typeCombo.valueProperty().addListener((obs, ancien, nouveau) -> suggererCibleType(nouveau));
        cibleTypeCombo.valueProperty().addListener((obs, ancien, nouveau) -> rafraichirValeursCible(nouveau));
    }

    private void suggererCibleType(Election.TypeElection type) {
        if (type == null) {
            return;
        }
        Election.CibleType suggestion = switch (type) {
            case DELEGUE -> Election.CibleType.CLASSE;
            case CHEF_DEPARTEMENT -> Election.CibleType.DEPARTEMENT;
            case DIRECTEUR_UFR, DIRECTEUR_ADJOINT_UFR -> Election.CibleType.UFR;
            case REPRESENTANT -> Election.CibleType.TOUS;
        };
        cibleTypeCombo.setValue(suggestion);
    }

    private void rafraichirValeursCible(Election.CibleType cibleType) {
        if (cibleType == null) {
            cibleValeurCombo.setItems(FXCollections.observableArrayList());
            cibleValeurCombo.setDisable(false);
            return;
        }
        switch (cibleType) {
            case CLASSE -> {
                cibleValeurCombo.setItems(FXCollections.observableArrayList(
                        uam.evote.utils.ReferentielUAM.listerToutesClasses()));
                cibleValeurCombo.setDisable(false);
            }
            case DEPARTEMENT -> {
                cibleValeurCombo.setItems(FXCollections.observableArrayList(
                        uam.evote.utils.ReferentielUAM.listerTousDepartements()));
                cibleValeurCombo.setDisable(false);
            }
            case UFR -> {
                cibleValeurCombo.setItems(FXCollections.observableArrayList(
                        uam.evote.utils.ReferentielUAM.listerUFR()));
                cibleValeurCombo.setDisable(false);
            }
            case TOUS -> {
                cibleValeurCombo.setItems(FXCollections.observableArrayList("toute l'université"));
                cibleValeurCombo.setValue("toute l'université");
                cibleValeurCombo.setDisable(true);
            }
        }
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
        cibleValeurCombo.setValue(election.getCibleValeur());
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
        election.setCibleValeur(cibleValeurCombo.getValue());
        election.setDateDebut(dateDebutPicker.getValue().atStartOfDay());
        election.setDateFin(dateFinPicker.getValue().atTime(23, 59));
        return election;
    }

    private boolean validerChamps() {
        if (titreField.getText().isBlank() || typeCombo.getValue() == null
                || cibleTypeCombo.getValue() == null || cibleValeurCombo.getValue() == null
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