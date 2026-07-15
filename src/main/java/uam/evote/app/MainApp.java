package uam.evote.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("UAM e-Vote — ça fonctionne !");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 500, 300);

        primaryStage.setTitle("UAM e-Vote");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}