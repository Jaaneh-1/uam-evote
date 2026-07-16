USE uam_evote;

SET @hash = '$2a$10$21envPocrGOw6qdckgthie72kTabXvFnfv26d4PGk3rIh3KbWKB0O';

INSERT INTO users (matricule, nom, prenom, email, login, password, role, classe, departement, ufr) VALUES
                                                                                                       ('ADM001', 'Diop', 'Fatou', 'fatou.diop@uam.edu.sn', 'admin', @hash, 'ADMIN', NULL, NULL, 'STA'),
                                                                                                       ('ETU001', 'Ndiaye', 'Moussa', 'moussa.ndiaye@uam.edu.sn', 'moussa.ndiaye', @hash, 'ETUDIANT', 'L3 Informatique', NULL, 'STA'),
                                                                                                       ('ETU002', 'Sarr', 'Aminata', 'aminata.sarr@uam.edu.sn', 'aminata.sarr', @hash, 'ETUDIANT', 'L3 Informatique', NULL, 'STA'),
                                                                                                       ('ENS001', 'Ba', 'Ousmane', 'ousmane.ba@uam.edu.sn', 'ousmane.ba', @hash, 'ENSEIGNANT', NULL, 'Informatique', 'STA');

INSERT INTO elections (titre, type_election, cible_type, cible_valeur, date_debut, date_fin, statut, created_by) VALUES
    ('Délégué L3 Informatique 2026', 'DELEGUE', 'CLASSE', 'L3 Informatique',
     '2026-09-01 08:00:00', '2026-09-03 18:00:00', 'EN_PREPARATION', 1);

INSERT INTO candidats (election_id, nom, prenom, profession, programme) VALUES
                                                                            (1, 'Ndiaye', 'Moussa', 'Étudiant L3', 'Meilleure communication entre étudiants et administration'),
                                                                            (1, 'Sarr', 'Aminata', 'Étudiante L3', 'Organisation d''événements et soutien pédagogique');