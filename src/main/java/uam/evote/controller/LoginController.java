package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uam.evote.model.User;
import uam.evote.service.AuthService;

import java.io.IOException;
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

        if (login.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Optional<User> resultat = authService.connexion(login, motDePasse);

            if (resultat.isPresent()) {
                User user = resultat.get();
                System.out.println("Connexion réussie : " + user);
                rediriger(user);
            } else {
                afficherErreur("Login ou mot de passe incorrect.");
            }

        } catch (RuntimeException e) {
            afficherErreur("Connexion au serveur impossible. Vérifiez votre réseau et réessayez.");
        }
    }

    private void rediriger(User user) {
        try {
            String fxmlPath;
            if (user.getRole() == User.Role.ADMIN) {
                fxmlPath = "/fxml/admin-dashboard.fxml";
            } else {
                fxmlPath = "/fxml/electeur-dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Transmet l'utilisateur connecté au bon Controller
            if (user.getRole() == User.Role.ADMIN) {
                AdminDashboardController controller = loader.getController();
                controller.initData(user);
            } else {
                ElecteurDashboardController controller = loader.getController();
                controller.initData(user);
            }

            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);

        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de l'écran suivant.");
            e.printStackTrace();
        }
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(!texte.isEmpty());
        messageErreur.setManaged(!texte.isEmpty());
    }

    @FXML
    private void handleScanQR() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/qr-scanner.fxml")
            );
            javafx.scene.Parent root = loader.load();

            QrScannerController controller = loader.getController();
            controller.setOnQrDetecte(user -> {
                System.out.println("Connexion par QR réussie : " + user);
                rediriger(user);
            });

            Stage popup = new Stage();
            popup.setTitle("Scanner QR Code");
            popup.setScene(new Scene(root));
            popup.showAndWait();

        } catch (IOException e) {
            afficherErreur("Erreur lors de l'ouverture du scanner.");
        }
    }
}