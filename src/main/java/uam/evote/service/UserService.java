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
}