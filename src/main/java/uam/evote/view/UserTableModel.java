package uam.evote.view;

import javafx.beans.property.SimpleStringProperty;
import uam.evote.model.User;

/**
 * Adapte un objet User pour l'affichage dans un TableView JavaFX.
 * On garde le vrai User en référence pour retrouver l'id lors des actions (modifier/supprimer).
 */
public class UserTableModel {

    private final User user;

    private final SimpleStringProperty matricule;
    private final SimpleStringProperty nom;
    private final SimpleStringProperty prenom;
    private final SimpleStringProperty email;
    private final SimpleStringProperty role;
    private final SimpleStringProperty classeOuDepartement;

    public UserTableModel(User user) {
        this.user = user;
        this.matricule = new SimpleStringProperty(user.getMatricule());
        this.nom = new SimpleStringProperty(user.getNom());
        this.prenom = new SimpleStringProperty(user.getPrenom());
        this.email = new SimpleStringProperty(user.getEmail());
        this.role = new SimpleStringProperty(user.getRole().name());

        String classeOuDept = user.getRole() == User.Role.ETUDIANT
                ? user.getClasse()
                : user.getDepartement();
        this.classeOuDepartement = new SimpleStringProperty(
                classeOuDept != null ? classeOuDept : "-"
        );
    }

    public User getUser() {
        return user;
    }

    public String getMatricule() {
        return matricule.get();
    }

    public String getNom() {
        return nom.get();
    }

    public String getPrenom() {
        return prenom.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getRole() {
        return role.get();
    }

    public String getClasseOuDepartement() {
        return classeOuDepartement.get();
    }
}