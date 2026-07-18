package uam.evote.view;

import javafx.beans.property.SimpleStringProperty;
import uam.evote.model.Election;

import java.time.format.DateTimeFormatter;

public class ElectionTableModel {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Election election;

    private final SimpleStringProperty titre;
    private final SimpleStringProperty type;
    private final SimpleStringProperty cible;
    private final SimpleStringProperty dateDebut;
    private final SimpleStringProperty dateFin;
    private final SimpleStringProperty statut;

    public ElectionTableModel(Election election) {
        this.election = election;
        this.titre = new SimpleStringProperty(election.getTitre());
        this.type = new SimpleStringProperty(election.getTypeElection().name());
        this.cible = new SimpleStringProperty(
                election.getCibleType().name() + " : " + election.getCibleValeur()
        );
        this.dateDebut = new SimpleStringProperty(election.getDateDebut().format(FORMAT));
        this.dateFin = new SimpleStringProperty(election.getDateFin().format(FORMAT));
        this.statut = new SimpleStringProperty(election.getStatut().name());
    }

    public Election getElection() {
        return election;
    }

    public String getTitre() {
        return titre.get();
    }

    public String getType() {
        return type.get();
    }

    public String getCible() {
        return cible.get();
    }

    public String getDateDebut() {
        return dateDebut.get();
    }

    public String getDateFin() {
        return dateFin.get();
    }

    public String getStatut() {
        return statut.get();
    }
}