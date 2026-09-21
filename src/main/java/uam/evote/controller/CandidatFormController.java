package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import uam.evote.model.Candidat;
import uam.evote.model.Election;
import uam.evote.model.User;
import uam.evote.service.CandidatService;
import uam.evote.service.ElectionService;
import uam.evote.service.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CandidatFormController {

    @FXML
    private Label titreLabel;
    @FXML
    private ImageView photoPreview;
    @FXML
    private ComboBox<User> utilisateurCombo;
    @FXML
    private TextField professionField;
    @FXML
    private TextArea programmeArea;
    @FXML
    private Label messageErreur;

    private final CandidatService candidatService = new CandidatService();
    private final UserService userService = new UserService();
    private final ElectionService electionService = new ElectionService();

    private Candidat candidatEnEdition;
    private Runnable onSuccess;
    private int electionId;
    private Election election;
    private String cheminPhotoChoisie;

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
        this.election = electionService.trouverParId(electionId).orElse(null);
        chargerUtilisateursEligibles();
    }

    @FXML
    public void initialize() {
        utilisateurCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                return user.getPrenom() + " " + user.getNom() + " (" + user.getMatricule() + ")";
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });

        // Petit confort : quand on choisit un utilisateur, on reprend sa photo de compte
        // par défaut (l'admin peut toujours la remplacer via "Choisir une photo").
        utilisateurCombo.valueProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null && (cheminPhotoChoisie == null || cheminPhotoChoisie.isBlank())) {
                cheminPhotoChoisie = nouveau.getPhoto();
                afficherPhoto(cheminPhotoChoisie);
            }
        });
    }

    /**
     * Ne propose comme candidats possibles que des utilisateurs réellement éligibles
     * à voter à cette élection (même règle que VoteService.voter() via Election.estEligible) :
     * un candidat doit appartenir à la classe/département/UFR ciblée, avec le bon rôle.
     * Un administrateur n'est jamais proposé, quel que soit le type d'élection.
     */
    private void chargerUtilisateursEligibles() {
        if (election == null) {
            return;
        }

        List<User> eligibles = new ArrayList<>();
        for (User user : userService.listerTous()) {
            if (user.getRole() != User.Role.ADMIN && election.estEligible(user)) {
                eligibles.add(user);
            }
        }

        utilisateurCombo.setItems(FXCollections.observableArrayList(eligibles));
    }

    public void setCandidatEnEdition(Candidat candidat) {
        this.candidatEnEdition = candidat;
        titreLabel.setText("Modifier le candidat");

        userService.trouverParId(candidat.getUserId()).ifPresent(user -> {
            boolean dejaPresent = utilisateurCombo.getItems().stream()
                    .anyMatch(u -> u.getId() == user.getId());
            if (!dejaPresent) {
                utilisateurCombo.getItems().add(user);
            }
            utilisateurCombo.setValue(user);
        });

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

        File fichierChoisi = fileChooser.showOpenDialog(utilisateurCombo.getScene().getWindow());
        if (fichierChoisi == null) {
            return;
        }

        try {
            Path dossierPhotos = uam.evote.utils.AppDataDir.get("photos");
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
        if (utilisateurCombo.getValue() == null) {
            afficherErreur("Veuillez sélectionner un utilisateur.");
            return;
        }

        try {
            User utilisateur = utilisateurCombo.getValue();

            Candidat candidat = new Candidat();
            candidat.setUserId(utilisateur.getId());
            candidat.setNom(utilisateur.getNom());
            candidat.setPrenom(utilisateur.getPrenom());
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
        Stage stage = (Stage) utilisateurCombo.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }
}