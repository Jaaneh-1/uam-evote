package uam.evote.service;

import uam.evote.dao.UserDAO;
import uam.evote.dao.UserDAOImpl;
import uam.evote.model.User;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.authService = new AuthService();
    }

    /**
     * Crée un nouvel utilisateur. Le mot de passe fourni en clair est haché avant stockage.
     * Retourne l'id généré, ou lève une exception si le login existe déjà.
     */
    public int creerUtilisateur(User user, String motDePasseClair) {
        if (userDAO.trouverParLogin(user.getLogin()).isPresent()) {
            throw new IllegalArgumentException("Ce login est déjà utilisé.");
        }

        String hash = authService.hacherMotDePasse(motDePasseClair);
        user.setPassword(hash);

        return userDAO.ajouter(user);
    }

    public void modifierUtilisateur(User user) {
        userDAO.modifier(user);
    }

    public void supprimerUtilisateur(int id) {
        userDAO.supprimer(id);
    }

    public List<User> listerTous() {
        return userDAO.trouverTous();
    }

    public List<User> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) {
            return listerTous();
        }
        return userDAO.rechercher(motCle);
    }

    public Optional<User> trouverParId(int id) {
        return userDAO.trouverParId(id);
    }

    /**
     * Importe une liste d'électeurs depuis un fichier CSV.
     * Format attendu (avec en-tête) : matricule,nom,prenom,email,login,role,classe,departement,ufr
     * Le mot de passe initial est généré automatiquement (= matricule), à faire changer ensuite.
     * Retourne un rapport listant les succès et les échecs (ex: login déjà pris).
     */
    public ImportRapport importerDepuisCSV(String cheminFichier) throws java.io.IOException {
        ImportRapport rapport = new ImportRapport();

        try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new java.io.FileReader(cheminFichier))) {
            List<String[]> lignes = reader.readAll();

            if (lignes.isEmpty()) {
                return rapport;
            }

            // On saute la première ligne (en-tête)
            for (int i = 1; i < lignes.size(); i++) {
                String[] colonnes = lignes.get(i);

                if (colonnes.length < 9) {
                    rapport.ajouterEchec("Ligne " + (i + 1), "Nombre de colonnes incorrect");
                    continue;
                }

                try {
                    User user = new User();
                    user.setMatricule(colonnes[0].trim());
                    user.setNom(colonnes[1].trim());
                    user.setPrenom(colonnes[2].trim());
                    user.setEmail(colonnes[3].trim());
                    user.setLogin(colonnes[4].trim());
                    user.setRole(User.Role.valueOf(colonnes[5].trim().toUpperCase()));
                    user.setClasse(colonnes[6].trim().isEmpty() ? null : colonnes[6].trim());
                    user.setDepartement(colonnes[7].trim().isEmpty() ? null : colonnes[7].trim());
                    user.setUfr(colonnes[8].trim());

                    // Mot de passe initial = matricule (l'électeur pourra le changer ensuite)
                    creerUtilisateur(user, user.getMatricule());

                    rapport.ajouterSucces(user.getLogin());

                } catch (IllegalArgumentException e) {
                    rapport.ajouterEchec(colonnes[4], e.getMessage());
                } catch (Exception e) {
                    rapport.ajouterEchec(colonnes.length > 4 ? colonnes[4] : "Ligne " + (i + 1),
                            "Erreur : " + e.getMessage());
                }
            }
        } catch (com.opencsv.exceptions.CsvException e) {
            throw new java.io.IOException("Fichier CSV invalide : " + e.getMessage());
        }

        return rapport;
    }

    /**
     * Rapport d'import : liste les logins créés avec succès et les échecs avec leur raison.
     */
    public static class ImportRapport {
        private final List<String> succes = new java.util.ArrayList<>();
        private final List<String> echecs = new java.util.ArrayList<>();

        public void ajouterSucces(String login) {
            succes.add(login);
        }

        public void ajouterEchec(String identifiant, String raison) {
            echecs.add(identifiant + " : " + raison);
        }

        public List<String> getSucces() {
            return succes;
        }

        public List<String> getEchecs() {
            return echecs;
        }

        public int getNbSucces() {
            return succes.size();
        }

        public int getNbEchecs() {
            return echecs.size();
        }
    }
}