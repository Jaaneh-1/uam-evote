package uam.evote.controller;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import uam.evote.model.User;
import uam.evote.service.AuthService;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Consumer;

public class QrScannerController {

    @FXML
    private Label statutLabel;
    @FXML
    private ImageView camView;

    private final AuthService authService = new AuthService();
    private Webcam webcam;
    private AnimationTimer timer;
    private Consumer<User> onQrDetecte;

    /**
     * Callback appelé quand un utilisateur a été identifié via son QR code scanné.
     */
    public void setOnQrDetecte(Consumer<User> callback) {
        this.onQrDetecte = callback;
    }

    @FXML
    public void initialize() {
        try {
            webcam = Webcam.getDefault();

            if (webcam == null) {
                statutLabel.setText("Aucune webcam détectée sur cet ordinateur.");
                return;
            }

            webcam.open();
            demarrerBoucleCapture();

        } catch (Exception e) {
            // Toute erreur webcam (pilote, permission...) ne bloque jamais l'application :
            // l'électeur pourra toujours se connecter avec login/mot de passe classique.
            statutLabel.setText("Webcam indisponible sur cet ordinateur. Utilisez la connexion classique.");
        }
    }

    private void demarrerBoucleCapture() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (webcam == null || !webcam.isOpen()) {
                    return;
                }

                BufferedImage image = webcam.getImage();
                if (image == null) {
                    return;
                }

                camView.setImage(SwingFXUtils.toFXImage(image, null));

                String contenu = tenterDecoderQR(image);
                if (contenu != null) {
                    traiterToken(contenu);
                }
            }
        };
        timer.start();
    }

    private String tenterDecoderQR(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null; // aucun QR détecté sur cette image, on continue la boucle
        }
    }

    private void traiterToken(String token) {
        Optional<User> userOpt = authService.connexionParToken(token);

        if (userOpt.isPresent()) {
            arreterCapture();
            User user = userOpt.get();

            Platform.runLater(() -> {
                if (onQrDetecte != null) {
                    onQrDetecte.accept(user);
                }
                fermerFenetre();
            });
        } else {
            statutLabel.setText("QR code non reconnu, réessayez.");
        }
    }

    @FXML
    private void handleAnnuler() {
        arreterCapture();
        fermerFenetre();
    }

    private void arreterCapture() {
        if (timer != null) {
            timer.stop();
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) camView.getScene().getWindow();
        stage.close();
    }
}