DROP TABLE IF EXISTS personne;

CREATE TABLE personne (
    login VARCHAR(50) PRIMARY KEY,
    mdp VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    adresse VARCHAR(255),
    email VARCHAR(100),
    tel VARCHAR(20),
    datenaiss DATE
);

INSERT INTO personne (login, mdp, nom, prenom, email) 
VALUES ('enzo', 'Azert59', 'Dewame', 'Enzo', 'enzo@example.com');

INSERT INTO personne (login, mdp, nom, prenom, email) 
VALUES ('emille', 'Maquillage59', 'Camard', 'emille', 'emille@example.com');

INSERT INTO personne (login, mdp, nom, prenom, email) 
VALUES ('aliocha', 'monmdp123', 'deflou', 'Aliocha', 'aliocha@example.com');