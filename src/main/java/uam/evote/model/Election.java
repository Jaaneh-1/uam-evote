package uam.evote.model;

import java.time.LocalDateTime;

public class Election {

    public enum TypeElection {
        DELEGUE, CHEF_DEPARTEMENT, DIRECTEUR_UFR, DIRECTEUR_ADJOINT_UFR, REPRESENTANT
    }

    public enum CibleType {
        CLASSE, DEPARTEMENT, UFR, TOUS
    }

    public enum Statut {
        EN_PREPARATION, OUVERTE, FERMEE
    }

    private int id;
    private String titre;
    private TypeElection typeElection;

    // Détermine qui a le droit de voter à cette élection
    private CibleType cibleType;
    private String cibleValeur; // ex: "L3 Informatique" si cibleType = CLASSE

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Statut statut;
    private int createdBy; // id de l'admin qui a créé l'élection

    public Election() {
    }

    public Election(String titre, TypeElection typeElection, CibleType cibleType,
                    String cibleValeur, LocalDateTime dateDebut, LocalDateTime dateFin,
                    Statut statut, int createdBy) {
        this.titre = titre;
        this.typeElection = typeElection;
        this.cibleType = cibleType;
        this.cibleValeur = cibleValeur;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.createdBy = createdBy;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public TypeElection getTypeElection() {
        return typeElection;
    }

    public void setTypeElection(TypeElection typeElection) {
        this.typeElection = typeElection;
    }

    public CibleType getCibleType() {
        return cibleType;
    }

    public void setCibleType(CibleType cibleType) {
        this.cibleType = cibleType;
    }

    public String getCibleValeur() {
        return cibleValeur;
    }

    public void setCibleValeur(String cibleValeur) {
        this.cibleValeur = cibleValeur;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Vérifie si un utilisateur donné a le droit de voter à cette élection,
     * en fonction de son rôle ET de sa classe/département/UFR.
     * C'est ici qu'on implémente les "Cas 1, 2, 3" du sujet : la cible (classe,
     * département, UFR) ne suffit pas, le rôle attendu pour ce type d'élection
     * doit aussi correspondre (ex: seuls les enseignants votent pour un Directeur d'UFR).
     */
    public boolean estEligible(User user) {
        if (!cibleCorrespond(user)) {
            return false;
        }
        return switch (typeElection) {
            case DELEGUE -> user.getRole() == User.Role.ETUDIANT;
            case CHEF_DEPARTEMENT, DIRECTEUR_UFR, DIRECTEUR_ADJOINT_UFR -> user.getRole() == User.Role.ENSEIGNANT;
            case REPRESENTANT -> true;
        };
    }

    private boolean cibleCorrespond(User user) {
        return switch (cibleType) {
            case CLASSE -> user.getClasse() != null && user.getClasse().equalsIgnoreCase(cibleValeur);
            case DEPARTEMENT -> user.getDepartement() != null && user.getDepartement().equalsIgnoreCase(cibleValeur);
            case UFR -> user.getUfr() != null && user.getUfr().equalsIgnoreCase(cibleValeur);
            case TOUS -> true;
        };
    }

    @Override
    public String toString() {
        return "Election{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", statut=" + statut +
                '}';
    }
}