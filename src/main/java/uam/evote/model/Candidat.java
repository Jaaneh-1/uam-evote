package uam.evote.model;

public class Candidat {

    private int id;
    private int electionId;
    private String nom;
    private String prenom;
    private String photo;       // chemin/nom du fichier image
    private String profession;  // profession ou fonction
    private String programme;   // programme électoral

    public Candidat() {
    }

    public Candidat(int electionId, String nom, String prenom,
                    String photo, String profession, String programme) {
        this.electionId = electionId;
        this.nom = nom;
        this.prenom = prenom;
        this.photo = photo;
        this.profession = profession;
        this.programme = programme;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "Candidat{" +
                "id=" + id +
                ", nomComplet='" + getNomComplet() + '\'' +
                '}';
    }
}