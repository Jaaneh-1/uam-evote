package uam.evote.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import uam.evote.model.User;

public class ElecteurDashboardController {

    @FXML
    private Label bienvenueLabel;

    public void initData(User user) {
        bienvenueLabel.setText("Bienvenue, " + user.getNomComplet() + " (" + user.getRole() + ")");
    }
}