DROP DATABASE IF EXISTS uam_evote;
CREATE DATABASE uam_evote CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE uam_evote;

CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       matricule VARCHAR(20) UNIQUE,
                       nom VARCHAR(50) NOT NULL,
                       prenom VARCHAR(50) NOT NULL,
                       email VARCHAR(100),
                       login VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'ETUDIANT', 'ENSEIGNANT') NOT NULL,
                       classe VARCHAR(100) NULL,
                       departement VARCHAR(100) NULL,
                       ufr VARCHAR(100) NULL,
                       qr_token VARCHAR(64) UNIQUE NULL,
                       photo VARCHAR(255) NULL,
                       date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE elections (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           titre VARCHAR(200) NOT NULL,
                           type_election ENUM(
        'DELEGUE', 'CHEF_DEPARTEMENT', 'DIRECTEUR_UFR',
        'DIRECTEUR_ADJOINT_UFR', 'REPRESENTANT'
    ) NOT NULL,
                           cible_type ENUM('CLASSE', 'DEPARTEMENT', 'UFR', 'TOUS') NOT NULL,
                           cible_valeur VARCHAR(100) NOT NULL,
                           date_debut DATETIME NOT NULL,
                           date_fin DATETIME NOT NULL,
                           statut ENUM('EN_PREPARATION', 'OUVERTE', 'FERMEE') DEFAULT 'EN_PREPARATION',
                           created_by INT,
                           FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE candidats (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           election_id INT NOT NULL,
                           nom VARCHAR(50) NOT NULL,
                           prenom VARCHAR(50) NOT NULL,
                           photo VARCHAR(255),
                           profession VARCHAR(100),
                           programme TEXT,
                           FOREIGN KEY (election_id) REFERENCES elections(id) ON DELETE CASCADE
);

CREATE TABLE votes (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       election_id INT NOT NULL,
                       candidat_id INT NOT NULL,
                       utilisateur_id INT NOT NULL,
                       date_vote DATETIME DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (election_id) REFERENCES elections(id),
                       FOREIGN KEY (candidat_id) REFERENCES candidats(id),
                       FOREIGN KEY (utilisateur_id) REFERENCES users(id),
                       UNIQUE (election_id, utilisateur_id)
);

CREATE INDEX idx_users_classe ON users(classe);
CREATE INDEX idx_users_departement ON users(departement);
CREATE INDEX idx_elections_statut ON elections(statut);