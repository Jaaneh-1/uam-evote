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

import uam.evote.utils.DatabaseConnection;
import uam.evote.utils.ServerConfig;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private TextField adresseServeurField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageErreur;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        adresseServeurField.setText(ServerConfig.lireAdresseServeur());
    }


    @FXML
    private void handleConnexion() {
        String adresseServeur = adresseServeurField.getText().trim();
        String login = loginField.getText().trim();
        String motDePasse = passwordField.getText();

        if (adresseServeur.isEmpty() || login.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs, y compris l'adresse du serveur.");
            return;
        }

        DatabaseConnection.setHost(adresseServeur);

        try {
            Optional<User> resultat = authService.connexion(login, motDePasse);

            if (resultat.isPresent()) {
                ServerConfig.sauvegarderAdresseServeur(adresseServeur);
                User user = resultat.get();
                System.out.println("Connexion réussie : " + user);
                rediriger(user);
            } else {
                afficherErreur("Login ou mot de passe incorrect.");
            }

        } catch (RuntimeException e) {
            afficherErreur("Connexion au serveur impossible. Vérifiez l'adresse du serveur et votre réseau.");
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
            stage.centerOnScreen();

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
        String adresseServeur = adresseServeurField.getText().trim();
        if (adresseServeur.isEmpty()) {
            afficherErreur("Veuillez renseigner l'adresse du serveur avant de scanner un QR code.");
            return;
        }
        DatabaseConnection.setHost(adresseServeur);

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/qr-scanner.fxml")
            );
            javafx.scene.Parent root = loader.load();

            QrScannerController controller = loader.getController();
            controller.setOnQrDetecte(user -> {
                ServerConfig.sauvegarderAdresseServeur(adresseServeur);
                System.out.println("Connexion par QR réussie : " + user);
                rediriger(user);
            });

            Stage popup = new Stage();
            popup.setTitle("Scanner QR Code");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            popup.setScene(scene);
            popup.showAndWait();

        } catch (IOException e) {
            afficherErreur("Erreur lors de l'ouverture du scanner.");
        }
    }
}