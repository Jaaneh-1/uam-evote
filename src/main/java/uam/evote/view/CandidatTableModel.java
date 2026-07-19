package uam.evote.view;

import javafx.beans.property.SimpleStringProperty;
import uam.evote.model.Candidat;

public class CandidatTableModel {

    private final Candidat candidat;

    private final SimpleStringProperty nom;
    private final SimpleStringProperty prenom;
    private final SimpleStringProperty profession;
    private final SimpleStringProperty programme;
    private final SimpleStringProperty photo;

    public CandidatTableModel(Candidat candidat) {
        this.candidat = candidat;
        this.nom = new SimpleStringProperty(candidat.getNom());
        this.prenom = new SimpleStringProperty(candidat.getPrenom());
        this.profession = new SimpleStringProperty(
                candidat.getProfession() != null ? candidat.getProfession() : "-"
        );
        this.programme = new SimpleStringProperty(
                candidat.getProgramme() != null ? candidat.getProgramme() : "-"
        );
        this.photo = new SimpleStringProperty(
                candidat.getPhoto() != null ? "Oui" : "Non"
        );
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public String getNom() {
        return nom.get();
    }

    public String getPrenom() {
        return prenom.get();
    }

    public String getProfession() {
        return profession.get();
    }

    public String getProgramme() {
        return programme.get();
    }

    public String getPhoto() {
        return photo.get();
    }
}