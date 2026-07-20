package uam.evote.service;

import org.mindrot.jbcrypt.BCrypt;
import uam.evote.dao.UserDAO;
import uam.evote.dao.UserDAOImpl;
import uam.evote.model.User;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Tente de connecter un utilisateur avec son login et mot de passe en clair.
     * Retourne l'utilisateur si les identifiants sont corrects, sinon Optional.empty().
     */
    public Optional<User> connexion(String login, String motDePasseClair) {
        Optional<User> userOpt = userDAO.trouverParLogin(login);

        if (userOpt.isEmpty()) {
            return Optional.empty(); // login inconnu
        }

        User user = userOpt.get();

        // Compare le mot de passe saisi avec le hash stocké en base
        boolean motDePasseCorrect = BCrypt.checkpw(motDePasseClair, user.getPassword());

        if (motDePasseCorrect) {
            return Optional.of(user);
        }
        return Optional.empty(); // mot de passe incorrect
    }

    /**
     * Hache un mot de passe en clair avec BCrypt, pour le stocker en base.
     * À utiliser lors de la création d'un nouvel utilisateur.
     */
    public String hacherMotDePasse(String motDePasseClair) {
        return BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());
    }

    /**
     * Génère (ou régénère) un jeton QR unique pour un utilisateur, et le stocke en base.
     */
    public String genererQrToken(User user) {
        String token = java.util.UUID.randomUUID().toString();
        userDAO.definirQrToken(user.getId(), token);
        return token;
    }

    /**
     * Connexion via un jeton scanné (QR code), sans mot de passe.
     */
    public Optional<User> connexionParToken(String token) {
        return userDAO.trouverParQrToken(token);
    }
}