package uam.evote.view;

import javafx.beans.property.SimpleStringProperty;
import uam.evote.model.Election;

import java.time.format.DateTimeFormatter;

public class ElectionEligibleTableModel {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Election election;

    private final SimpleStringProperty titre;
    private final SimpleStringProperty type;
    private final SimpleStringProperty dateFin;
    private final SimpleStringProperty statutVote;

    public ElectionEligibleTableModel(Election election, boolean dejaVote) {
        this.election = election;
        this.titre = new SimpleStringProperty(election.getTitre());
        this.type = new SimpleStringProperty(election.getTypeElection().name());
        this.dateFin = new SimpleStringProperty(election.getDateFin().format(FORMAT));
        this.statutVote = new SimpleStringProperty(dejaVote ? "Déjà voté ✓" : "Vous pouvez voter");
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

    public String getDateFin() {
        return dateFin.get();
    }

    public String getStatutVote() {
        return statutVote.get();
    }
}