package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uam.evote.model.User;
import uam.evote.service.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class UserFormController {

    @FXML
    private Label titreLabel;
    @FXML
    private ImageView photoPreview;
    @FXML
    private TextField matriculeField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<User.Role> roleCombo;
    @FXML
    private ComboBox<String> ufrCombo;
    @FXML
    private Label departementLabel;
    @FXML
    private ComboBox<String> departementCombo;
    @FXML
    private Label classeLabel;
    @FXML
    private ComboBox<String> classeCombo;
    @FXML
    private Label messageErreur;

    private final UserService userService = new UserService();

    private User userEnEdition;
    private Runnable onSuccess;
    private String cheminPhotoChoisie;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(User.Role.values()));
        ufrCombo.setItems(FXCollections.observableArrayList(uam.evote.utils.ReferentielUAM.listerUFR()));

        ufrCombo.valueProperty().addListener((obs, ancien, nouveau) -> rafraichirDepartements());
        departementCombo.valueProperty().addListener((obs, ancien, nouveau) -> rafraichirClasses());
        roleCombo.valueProperty().addListener((obs, ancien, nouveau) -> mettreAJourVisibiliteClasse());

        mettreAJourVisibiliteClasse();
    }

    private void rafraichirDepartements() {
        String ufr = ufrCombo.getValue();
        departementCombo.setItems(FXCollections.observableArrayList(
                ufr == null ? java.util.List.of() : uam.evote.utils.ReferentielUAM.listerDepartements(ufr)
        ));
    }

    private void rafraichirClasses() {
        String ufr = ufrCombo.getValue();
        String departement = departementCombo.getValue();
        classeCombo.setItems(FXCollections.observableArrayList(
                (ufr == null || departement == null)
                        ? java.util.List.of()
                        : uam.evote.utils.ReferentielUAM.listerClasses(ufr, departement)
        ));
    }

    private void mettreAJourVisibiliteClasse() {
        boolean etudiant = roleCombo.getValue() == User.Role.ETUDIANT;
        classeLabel.setVisible(etudiant);
        classeLabel.setManaged(etudiant);
        classeCombo.setVisible(etudiant);
        classeCombo.setManaged(etudiant);
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setUserEnEdition(User user) {
        this.userEnEdition = user;
        titreLabel.setText("Modifier l'utilisateur");

        matriculeField.setText(user.getMatricule());
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        loginField.setText(user.getLogin());
        roleCombo.setValue(user.getRole());

        ufrCombo.setValue(user.getUfr());
        if (user.getRole() == User.Role.ETUDIANT) {
            departementCombo.setValue(
                    uam.evote.utils.ReferentielUAM.trouverDepartementDeClasse(user.getUfr(), user.getClasse())
            );
            classeCombo.setValue(user.getClasse());
        } else if (user.getRole() == User.Role.ENSEIGNANT) {
            departementCombo.setValue(user.getDepartement());
        }

        cheminPhotoChoisie = user.getPhoto();

        passwordField.setPromptText("Laisser vide pour ne pas changer");
        afficherPhoto(cheminPhotoChoisie);
    }

    @FXML
    private void handleChoisirPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File fichierChoisi = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (fichierChoisi == null) {
            return;
        }

        try {
            Path dossierPhotos = uam.evote.utils.AppDataDir.get("profils");
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
        if (!validerChamps()) {
            return;
        }

        try {
            if (userEnEdition == null) {
                creerNouvelUtilisateur();
            } else {
                modifierUtilisateurExistant();
            }

            if (onSuccess != null) {
                onSuccess.run();
            }
            fermerFenetre();

        } catch (IllegalArgumentException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void creerNouvelUtilisateur() {
        if (passwordField.getText().isEmpty()) {
            afficherErreur("Le mot de passe est obligatoire pour un nouvel utilisateur.");
            return;
        }

        User user = construireUserDepuisFormulaire();
        userService.creerUtilisateur(user, passwordField.getText());
    }

    private void modifierUtilisateurExistant() {
        User user = construireUserDepuisFormulaire();
        user.setId(userEnEdition.getId());
        userService.modifierUtilisateur(user);
    }

    private User construireUserDepuisFormulaire() {
        User user = new User();
        user.setMatricule(matriculeField.getText().trim());
        user.setNom(nomField.getText().trim());
        user.setPrenom(prenomField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setLogin(loginField.getText().trim());
        user.setRole(roleCombo.getValue());
        user.setUfr(ufrCombo.getValue());
        user.setPhoto(cheminPhotoChoisie);

        if (roleCombo.getValue() == User.Role.ETUDIANT) {
            user.setClasse(classeCombo.getValue());
        } else if (roleCombo.getValue() == User.Role.ENSEIGNANT) {
            user.setDepartement(departementCombo.getValue());
        }

        return user;
    }

    private boolean validerChamps() {
        if (nomField.getText().isBlank() || prenomField.getText().isBlank()
                || loginField.getText().isBlank() || roleCombo.getValue() == null
                || ufrCombo.getValue() == null) {
            afficherErreur("Veuillez remplir au moins Nom, Prénom, Login, Rôle et UFR.");
            return false;
        }
        if (roleCombo.getValue() == User.Role.ETUDIANT && classeCombo.getValue() == null) {
            afficherErreur("Veuillez sélectionner la classe de l'étudiant.");
            return false;
        }
        if (roleCombo.getValue() == User.Role.ENSEIGNANT && departementCombo.getValue() == null) {
            afficherErreur("Veuillez sélectionner le département de l'enseignant.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) matriculeField.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }
}