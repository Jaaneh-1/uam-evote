package uam.evote.model;

public class User {

    public enum Role {
        ADMIN, ETUDIANT, ENSEIGNANT
    }

    private int id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String login;
    private String password; // mot de passe haché (BCrypt), jamais en clair
    private Role role;
    private String classe;       // rempli si role = ETUDIANT
    private String departement;  // rempli si role = ENSEIGNANT
    private String ufr;

    // Constructeur vide (nécessaire pour construire l'objet puis le remplir via les setters,
    // notamment quand on lit une ligne de résultat SQL)
    public User() {
    }

    // Constructeur pratique pour créer un nouvel utilisateur en une fois
    public User(String matricule, String nom, String prenom, String email,
                String login, String password, Role role,
                String classe, String departement, String ufr) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.login = login;
        this.password = password;
        this.role = role;
        this.classe = classe;
        this.departement = departement;
        this.ufr = ufr;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getUfr() {
        return ufr;
    }

    public void setUfr(String ufr) {
        this.ufr = ufr;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", nomComplet='" + getNomComplet() + '\'' +
                ", role=" + role +
                '}';
    }
}