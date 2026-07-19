package uam.evote.controller;
import javafx.stage.Stage;
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
import uam.evote.view.ElectionTableModel;

import uam.evote.model.Election;
import uam.evote.model.Candidat;
import uam.evote.service.CandidatService;
import uam.evote.view.CandidatTableModel;
import uam.evote.service.AuthService;

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
    @FXML
    private TableColumn<UserTableModel, String> colUfr;

    private final UserService userService = new UserService();
    private User utilisateurConnecte;



    @FXML
    private TableView<ElectionTableModel> tableElections;
    @FXML
    private TableColumn<ElectionTableModel, String> colTitre;
    @FXML
    private TableColumn<ElectionTableModel, String> colType;
    @FXML
    private TableColumn<ElectionTableModel, String> colCible;
    @FXML
    private TableColumn<ElectionTableModel, String> colDateDebut;
    @FXML
    private TableColumn<ElectionTableModel, String> colDateFin;
    @FXML
    private TableColumn<ElectionTableModel, String> colStatutElection;

    private final uam.evote.service.ElectionService electionService = new uam.evote.service.ElectionService();



    @FXML
    private ComboBox<Election> electionCandidatsCombo;
    @FXML
    private TableView<CandidatTableModel> tableCandidats;
    @FXML
    private TableColumn<CandidatTableModel, String> colNomCandidat;
    @FXML
    private TableColumn<CandidatTableModel, String> colPrenomCandidat;
    @FXML
    private TableColumn<CandidatTableModel, String> colProfession;
    @FXML
    private TableColumn<CandidatTableModel, String> colProgramme;
    @FXML
    private TableColumn<CandidatTableModel, String> colPhotoCandidat;

    private final CandidatService candidatService = new CandidatService();



    @FXML
    private TableView<ElectionTableModel> tableHistorique;
    @FXML
    private TableColumn<ElectionTableModel, String> colTitreHisto;
    @FXML
    private TableColumn<ElectionTableModel, String> colTypeHisto;
    @FXML
    private TableColumn<ElectionTableModel, String> colCibleHisto;
    @FXML
    private TableColumn<ElectionTableModel, String> colDateFinHisto;



    private final uam.evote.service.CarteService carteService = new uam.evote.service.CarteService();
    private final uam.evote.service.QrCodeService qrCodeService = new uam.evote.service.QrCodeService();








    @FXML
    public void initialize() {
        // Relie chaque colonne à la méthode getter correspondante de UserTableModel
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classeOuDepartement"));
        colUfr.setCellValueFactory(new PropertyValueFactory<>("ufr"));

        chargerUtilisateurs();

        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCible.setCellValueFactory(new PropertyValueFactory<>("cible"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatutElection.setCellValueFactory(new PropertyValueFactory<>("statut"));

        chargerElections();


        colNomCandidat.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenomCandidat.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colProfession.setCellValueFactory(new PropertyValueFactory<>("profession"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));
        colPhotoCandidat.setCellValueFactory(new PropertyValueFactory<>("photo"));

        electionCandidatsCombo.setConverter(new javafx.util.StringConverter<Election>() {
            @Override
            public String toString(Election election) {
                return election == null ? "" : election.getTitre();
            }
            @Override
            public Election fromString(String string) {
                return null;
            }
        });
        electionCandidatsCombo.setItems(FXCollections.observableArrayList(electionService.listerToutes()));

        colTitreHisto.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTypeHisto.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCibleHisto.setCellValueFactory(new PropertyValueFactory<>("cible"));
        colDateFinHisto.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        chargerHistorique();
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
        ouvrirFormulaire(null);
    }

    @FXML
    private void handleModifier() {
        UserTableModel selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un utilisateur à modifier.");
            return;
        }
        ouvrirFormulaire(selection.getUser());
    }

    private void ouvrirFormulaire(User userAModifier) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/user-form.fxml")
            );
            javafx.scene.Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setOnSuccess(this::chargerUtilisateurs);

            if (userAModifier != null) {
                controller.setUserEnEdition(userAModifier);
            }

            Stage popup = new Stage();
            popup.setTitle(userAModifier == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur");
            popup.setScene(new javafx.scene.Scene(root));
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de l'ouverture du formulaire.");
            e.printStackTrace();
        }
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


    private void chargerElections() {
        List<uam.evote.model.Election> elections = electionService.listerToutes();
        ObservableList<uam.evote.view.ElectionTableModel> data = FXCollections.observableArrayList(
                elections.stream().map(uam.evote.view.ElectionTableModel::new).collect(Collectors.toList())
        );
        tableElections.setItems(data);
    }

    @FXML
    private void handleAjouterElection() {
        ouvrirFormulaireElection(null);
    }

    @FXML
    private void handleModifierElection() {
        uam.evote.view.ElectionTableModel selection = tableElections.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection à modifier.");
            return;
        }
        ouvrirFormulaireElection(selection.getElection());
    }

    private void ouvrirFormulaireElection(uam.evote.model.Election electionAModifier) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/election-form.fxml")
            );
            javafx.scene.Parent root = loader.load();

            uam.evote.controller.ElectionFormController controller = loader.getController();
            controller.setOnSuccess(this::chargerElections);
            controller.setAdminId(utilisateurConnecte.getId());

            if (electionAModifier != null) {
                controller.setElectionEnEdition(electionAModifier);
            }

            Stage popup = new Stage();
            popup.setTitle(electionAModifier == null ? "Créer une élection" : "Modifier l'élection");
            popup.setScene(new javafx.scene.Scene(root));
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de l'ouverture du formulaire.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOuvrirScrutin() {
        uam.evote.view.ElectionTableModel selection = tableElections.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection.");
            return;
        }
        try {
            electionService.ouvrirScrutin(selection.getElection().getId());
            chargerElections();
        } catch (IllegalStateException e) {
            afficherMessage(e.getMessage());
        }
    }

    @FXML
    private void handleFermerScrutin() {
        uam.evote.view.ElectionTableModel selection = tableElections.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection.");
            return;
        }
        try {
            electionService.fermerScrutin(selection.getElection().getId());
            chargerElections();
            chargerHistorique();
        } catch (IllegalStateException e) {
            afficherMessage(e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerElection() {
        uam.evote.view.ElectionTableModel selection = tableElections.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'élection \"" + selection.getTitre() + "\" ?");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                electionService.supprimerElection(selection.getElection().getId());
                chargerElections();
            }
        });
    }


    @FXML
    private void handleChangerElectionCandidats() {
        chargerCandidats();
    }

    private void chargerCandidats() {
        Election election = electionCandidatsCombo.getValue();
        if (election == null) {
            tableCandidats.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Candidat> candidats = candidatService.listerParElection(election.getId());
        ObservableList<CandidatTableModel> data = FXCollections.observableArrayList(
                candidats.stream().map(CandidatTableModel::new).collect(Collectors.toList())
        );
        tableCandidats.setItems(data);
    }

    @FXML
    private void handleAjouterCandidat() {
        Election election = electionCandidatsCombo.getValue();
        if (election == null) {
            afficherMessage("Veuillez d'abord sélectionner une élection.");
            return;
        }
        ouvrirFormulaireCandidat(null, election.getId());
    }

    @FXML
    private void handleModifierCandidat() {
        CandidatTableModel selection = tableCandidats.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un candidat à modifier.");
            return;
        }
        ouvrirFormulaireCandidat(selection.getCandidat(), selection.getCandidat().getElectionId());
    }

    private void ouvrirFormulaireCandidat(Candidat candidatAModifier, int electionId) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/candidat-form.fxml")
            );
            javafx.scene.Parent root = loader.load();

            CandidatFormController controller = loader.getController();
            controller.setOnSuccess(this::chargerCandidats);
            controller.setElectionId(electionId);

            if (candidatAModifier != null) {
                controller.setCandidatEnEdition(candidatAModifier);
            }

            Stage popup = new Stage();
            popup.setTitle(candidatAModifier == null ? "Ajouter un candidat" : "Modifier le candidat");
            popup.setScene(new javafx.scene.Scene(root));
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de l'ouverture du formulaire.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimerCandidat() {
        CandidatTableModel selection = tableCandidats.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un candidat à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le candidat " + selection.getNom() + " " + selection.getPrenom() + " ?");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                candidatService.supprimerCandidat(selection.getCandidat().getId());
                chargerCandidats();
            }
        });
    }

    @FXML
    private void handleVoirResultats() {
        ElectionTableModel selection = tableElections.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/resultats.fxml")
            );
            javafx.scene.Parent root = loader.load();

            uam.evote.controller.ResultatsController controller = loader.getController();
            controller.initData(selection.getElection());

            Stage popup = new Stage();
            popup.setTitle("Résultats - " + selection.getTitre());
            popup.setScene(new javafx.scene.Scene(root, 820, 500));
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de l'ouverture des résultats.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleImporterCSV() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Importer des électeurs depuis un CSV");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        java.io.File fichier = fileChooser.showOpenDialog(tableUtilisateurs.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        try {
            UserService.ImportRapport rapport = userService.importerDepuisCSV(fichier.getAbsolutePath());

            StringBuilder message = new StringBuilder();
            message.append(rapport.getNbSucces()).append(" utilisateur(s) importé(s) avec succès.\n");

            if (rapport.getNbEchecs() > 0) {
                message.append(rapport.getNbEchecs()).append(" échec(s) :\n");
                for (String echec : rapport.getEchecs()) {
                    message.append("- ").append(echec).append("\n");
                }
            }

            Alert alert = new Alert(
                    rapport.getNbEchecs() == 0 ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING,
                    message.toString()
            );
            alert.setHeaderText("Résultat de l'import");
            alert.showAndWait();

            chargerUtilisateurs();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        }
    }

    private void chargerHistorique() {
        List<Election> historique = electionService.listerHistorique();
        ObservableList<ElectionTableModel> data = FXCollections.observableArrayList(
                historique.stream().map(ElectionTableModel::new).collect(Collectors.toList())
        );
        tableHistorique.setItems(data);
    }

    @FXML
    private void handleVoirResultatsHistorique() {
        ElectionTableModel selection = tableHistorique.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner une élection.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/resultats.fxml")
            );
            javafx.scene.Parent root = loader.load();

            uam.evote.controller.ResultatsController controller = loader.getController();
            controller.initData(selection.getElection());

            Stage popup = new Stage();
            popup.setTitle("Résultats - " + selection.getTitre());
            popup.setScene(new javafx.scene.Scene(root, 820, 500));
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.showAndWait();

        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de l'ouverture des résultats.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenererCarte() {
        UserTableModel selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un utilisateur.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer la carte d'électeur");
        fileChooser.setInitialFileName("carte_" + selection.getUser().getMatricule() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        java.io.File fichier = fileChooser.showSaveDialog(tableUtilisateurs.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        try {
            carteService.genererCarte(selection.getUser(), fichier.getAbsolutePath());
            afficherMessage("Carte générée : " + fichier.getName());
        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de la génération de la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleGenererQR() {
        UserTableModel selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Veuillez sélectionner un utilisateur.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le QR Code");
        fileChooser.setInitialFileName("qr_" + selection.getUser().getMatricule() + ".png");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Images PNG", "*.png")
        );

        java.io.File fichier = fileChooser.showSaveDialog(tableUtilisateurs.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        try {
            AuthService authService = new AuthService();
            String token = authService.genererQrToken(selection.getUser());
            qrCodeService.genererImagePNG(token, fichier.getAbsolutePath(), 300);
            afficherMessage("QR Code généré : " + fichier.getName());
        } catch (java.io.IOException e) {
            afficherMessage("Erreur lors de la génération du QR Code : " + e.getMessage());
        }
    }
}