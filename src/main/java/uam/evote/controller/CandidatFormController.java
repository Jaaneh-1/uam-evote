package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uam.evote.model.Candidat;
import uam.evote.service.CandidatService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class CandidatFormController {

    @FXML
    private Label titreLabel;
    @FXML
    private ImageView photoPreview;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField professionField;
    @FXML
    private TextArea programmeArea;
    @FXML
    private Label messageErreur;

    private final CandidatService candidatService = new CandidatService();

    private Candidat candidatEnEdition;
    private Runnable onSuccess;
    private int electionId;
    private String cheminPhotoChoisie;

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public void setCandidatEnEdition(Candidat candidat) {
        this.candidatEnEdition = candidat;
        titreLabel.setText("Modifier le candidat");

        nomField.setText(candidat.getNom());
        prenomField.setText(candidat.getPrenom());
        professionField.setText(candidat.getProfession());
        programmeArea.setText(candidat.getProgramme());
        cheminPhotoChoisie = candidat.getPhoto();

        afficherPhoto(cheminPhotoChoisie);
    }

    @FXML
    private void handleChoisirPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File fichierChoisi = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (fichierChoisi == null) {
            return;
        }

        try {
            // Dossier local où on stocke toutes les photos de candidats (créé s'il n'existe pas)
            Path dossierPhotos = Paths.get("data", "photos");
            Files.createDirectories(dossierPhotos);

            String extension = fichierChoisi.getName().substring(fichierChoisi.getName().lastIndexOf('.'));
            String nomFichierUnique = UUID.randomUUID() + extension;
            Path destination = dossierPhotos.resolve(nomFichierUnique);

            Files.copy(fichierChoisi.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            cheminPhotoChoisie = destination.toString();
            afficherPhoto(cheminPhotoChoisie);

        } catch (IOException e) {
            afficherErreur("Erreur lors de la copie de la photo.");
        }
    }

    private void afficherPhoto(String chemin) {
        if (chemin == null || chemin.isBlank()) {
            photoPreview.setImage(null);
            return;
        }
        File fichier = new File(chemin);
        if (fichier.exists()) {
            photoPreview.setImage(new Image(fichier.toURI().toString()));
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (nomField.getText().isBlank() || prenomField.getText().isBlank()) {
            afficherErreur("Le nom et le prénom sont obligatoires.");
            return;
        }

        try {
            Candidat candidat = new Candidat();
            candidat.setNom(nomField.getText().trim());
            candidat.setPrenom(prenomField.getText().trim());
            candidat.setProfession(professionField.getText().trim());
            candidat.setProgramme(programmeArea.getText().trim());
            candidat.setPhoto(cheminPhotoChoisie);

            if (candidatEnEdition == null) {
                candidat.setElectionId(electionId);
                candidatService.ajouterCandidat(candidat);
            } else {
                candidat.setId(candidatEnEdition.getId());
                candidat.setElectionId(candidatEnEdition.getElectionId());
                candidatService.modifierCandidat(candidat);
            }

            if (onSuccess != null) {
                onSuccess.run();
            }
            fermerFenetre();

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }
}