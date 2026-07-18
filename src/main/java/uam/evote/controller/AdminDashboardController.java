package uam.evote.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import uam.evote.model.User;
import uam.evote.service.UserService;
import uam.evote.view.UserTableModel;

import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML
    private Label bienvenueLabel;

    @FXML
    private TextField rechercheField;

    @FXML
    private TableView<UserTableModel> tableUtilisateurs;

    @FXML
    private TableColumn<UserTableModel, String> colMatricule;
    @FXML
    private TableColumn<UserTableModel, String> colNom;
    @FXML
    private TableColumn<UserTableModel, String> colPrenom;
    @FXML
    private TableColumn<UserTableModel, String> colEmail;
    @FXML
    private TableColumn<UserTableModel, String> colRole;
    @FXML
    private TableColumn<UserTableModel, String> colClasse;

    private final UserService userService = new UserService();
    private User utilisateurConnecte;

    @FXML
    public void initialize() {
        // Relie chaque colonne à la méthode getter correspondante de UserTableModel
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classeOuDepartement"));

        chargerUtilisateurs();
    }

    public void initData(User user) {
        this.utilisateurConnecte = user;
        bienvenueLabel.setText("Bienvenue, " + user.getNomComplet() + " (Administrateur)");
    }

    private void chargerUtilisateurs() {
        List<User> utilisateurs = userService.listerTous();
        ObservableList<UserTableModel> data = FXCollections.observableArrayList(
                utilisateurs.stream().map(UserTableModel::new).collect(Collectors.toList())
        );
        tableUtilisateurs.setItems(data);
    }

    @FXML
    private void handleActualiser() {
        rechercheField.clear();
        chargerUtilisateurs();
    }

    @FXML
    private void handleRecherche() {
        String motCle = rechercheField.getText();
        List<User> resultats = userService.rechercher(motCle);
        ObservableList<UserTableModel> data = FXCollections.observableArrayList(
                resultats.stream().map(UserTableModel::new).collect(Collectors.toList())
        );
        tableUtilisateurs.setItems(data);
    }

    @FXML
    private void handleAjouter() {
        afficherMessage("La fenêtre d'ajout sera codée à l'étape suivante.");
    }

    @FXML
    private void handleModifier() {
        UserTableModel selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un utilisateur à modifier.");
            return;
        }
        afficherMessage("La fenêtre de modification sera codée à l'étape suivante.");
    }

    @FXML
    private void handleSupprimer() {
        UserTableModel selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + selection.getNom() + " " + selection.getPrenom() + " ?");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                userService.supprimerUtilisateur(selection.getUser().getId());
                chargerUtilisateurs();
            }
        });
    }

    private void afficherMessage(String texte) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, texte);
        alert.showAndWait();
    }
}