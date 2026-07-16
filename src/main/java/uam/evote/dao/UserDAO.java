package uam.evote.dao;

import uam.evote.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    // Créer un nouvel utilisateur, retourne son id généré
    int ajouter(User user);

    // Modifier un utilisateur existant
    void modifier(User user);

    // Supprimer un utilisateur par son id
    void supprimer(int id);

    // Rechercher un utilisateur par son id
    Optional<User> trouverParId(int id);

    // Rechercher un utilisateur par son login (utilisé pour l'authentification)
    Optional<User> trouverParLogin(String login);

    // Lister tous les utilisateurs
    List<User> trouverTous();

    // Rechercher des utilisateurs par mot-clé (nom, prénom ou matricule)
    List<User> rechercher(String motCle);
}