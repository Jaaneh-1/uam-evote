package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import uam.evote.model.User;
import uam.evote.service.AuthService;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageErreur;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleConnexion() {
        String login = loginField.getText().trim();
        String motDePasse = passwordField.getText();

        // Validation simple avant même d'aller chercher en base
        if (login.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        Optional<User> resultat = authService.connexion(login, motDePasse);

        if (resultat.isPresent()) {
            User user = resultat.get();
            System.out.println("Connexion réussie : " + user);
            // TODO : rediriger vers le bon tableau de bord selon user.getRole()
            afficherErreur(""); // efface le message d'erreur éventuel
            messageErreur.setVisible(false);
            messageErreur.setManaged(false);
        } else {
            afficherErreur("Login ou mot de passe incorrect.");
        }
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(!texte.isEmpty());
        messageErreur.setManaged(!texte.isEmpty());
    }
}