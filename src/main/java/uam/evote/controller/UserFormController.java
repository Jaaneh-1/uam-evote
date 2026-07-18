package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uam.evote.model.User;
import uam.evote.service.UserService;

public class UserFormController {

    @FXML
    private Label titreLabel;
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
    private TextField classeDeptField;
    @FXML
    private TextField ufrField;
    @FXML
    private Label messageErreur;

    private final UserService userService = new UserService();

    private User userEnEdition; // null si on est en mode "Ajout"
    private Runnable onSuccess; // callback pour rafraîchir la liste après succès

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(User.Role.values()));
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    /**
     * Prépare le formulaire en mode "Modification", pré-rempli avec les données existantes.
     * Ne pas appeler cette méthode = mode "Ajout" (formulaire vide).
     */
    public void setUserEnEdition(User user) {
        this.userEnEdition = user;
        titreLabel.setText("Modifier l'utilisateur");

        matriculeField.setText(user.getMatricule());
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        loginField.setText(user.getLogin());
        roleCombo.setValue(user.getRole());
        classeDeptField.setText(
                user.getRole() == User.Role.ETUDIANT ? user.getClasse() : user.getDepartement()
        );
        ufrField.setText(user.getUfr());

        // En modification, le mot de passe reste optionnel (on ne le change que si rempli)
        passwordField.setPromptText("Laisser vide pour ne pas changer");
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
        // Note : le changement de mot de passe en modification sera ajouté ultérieurement si besoin
    }

    private User construireUserDepuisFormulaire() {
        User user = new User();
        user.setMatricule(matriculeField.getText().trim());
        user.setNom(nomField.getText().trim());
        user.setPrenom(prenomField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setLogin(loginField.getText().trim());
        user.setRole(roleCombo.getValue());
        user.setUfr(ufrField.getText().trim());

        if (roleCombo.getValue() == User.Role.ETUDIANT) {
            user.setClasse(classeDeptField.getText().trim());
        } else if (roleCombo.getValue() == User.Role.ENSEIGNANT) {
            user.setDepartement(classeDeptField.getText().trim());
        }

        return user;
    }

    private boolean validerChamps() {
        if (nomField.getText().isBlank() || prenomField.getText().isBlank()
                || loginField.getText().isBlank() || roleCombo.getValue() == null) {
            afficherErreur("Veuillez remplir au moins Nom, Prénom, Login et Rôle.");
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