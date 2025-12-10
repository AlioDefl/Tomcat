# Application Web Java Servlet - Gestion des Utilisateurs avec Rôles

Application web Java développée avec Jakarta Servlet API permettant la gestion des utilisateurs avec différents niveaux de privilèges.

## Fonctionnalités

### Authentification
- Système de connexion sécurisé avec session
- Gestion de la déconnexion
- Protection des pages par vérification de session

### Gestion des Rôles

#### Rôle `util` (Utilisateur)
- Consultation de ses propres coordonnées uniquement
- Modification de ses propres informations uniquement
- Pas d'accès aux données des autres utilisateurs

#### Rôle `admin` (Administrateur)
- Vue liste complète de tous les utilisateurs
- Consultation des coordonnées de n'importe quel utilisateur
- Modification des informations de n'importe quel utilisateur
- Navigation facilitée entre les utilisateurs

## Technologies Utilisées

- **Backend** : Java avec Jakarta Servlet API
- **Base de données** : PostgreSQL
- **Serveur** : Apache Tomcat 11
- **Frontend** : HTML/CSS

## Structure de la Base de Données

```sql
CREATE TABLE personne (
    login VARCHAR(50) PRIMARY KEY,
    mdp VARCHAR(255) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    adresse VARCHAR(255),
    email VARCHAR(100),
    tel VARCHAR(20),
    datenaiss DATE,
    role VARCHAR(10) DEFAULT 'util'
);
```

## Installation

### Prérequis
- Java 17 ou supérieur
- Apache Tomcat 11
- PostgreSQL
- Driver JDBC PostgreSQL

### Configuration

1. Cloner le repository
```bash
git clone https://github.com/AlioDefl/Tomcat.git
```

2. Configurer la base de données dans `WEB-INF/config.prop`
```properties
driver=org.postgresql.Driver
url=jdbc:postgresql://localhost:5432/votre_base
login=votre_login
password=votre_mot_de_passe
```

3. Créer la table dans PostgreSQL
```bash
psql -U votre_user -d votre_base -f personne.sql
```

4. Compiler les servlets
```bash
cd WEB-INF/src
javac -cp "../../lib/servlet-api.jar:." *.java -d ../classes
```

5. Déployer sur Tomcat
```bash
cp -r . $TOMCAT_HOME/webapps/votre_app/
```

## Utilisation

### URL d'accès
```
http://localhost:8080/vide/login.html
```

### Comptes de test

#### Administrateurs
- **aliocha** / monmdp123
- **enzo** / Azert59

#### Utilisateurs
- **testuser** / password123
- **emille** / Maquillage59

### Changement de rôle

Pour modifier le rôle d'un utilisateur dans la base :
```sql
UPDATE personne SET role = 'admin' WHERE login = 'nom_utilisateur';
```

Le changement est effectif dès la prochaine connexion (après déconnexion/reconnexion).

## Pages et Servlets

### Pages HTML
- `login.html` - Page de connexion
- `menu.html` - Menu principal
- `Saisie.html` - Formulaire de saisie

### Servlets Java
- `Authent.java` - Gestion de l'authentification
- `ServletLecture.java` - Consultation des coordonnées (avec gestion des rôles)
- `ServletModif.java` - Modification des coordonnées (avec gestion des rôles)
- `ServletMenu.java` - Génération du menu dynamique
- `ServletDeconnecte.java` - Gestion de la déconnexion

## Sécurité

- Utilisation de PreparedStatement pour prévenir les injections SQL
- Vérification systématique des sessions
- Contrôle d'accès basé sur les rôles
- Les utilisateurs `util` ne peuvent jamais accéder aux données des autres, même en manipulant les URL

## Principe de Fonctionnement

### Pour un utilisateur `util`
1. Se connecte via `login.html`
2. Accède au menu
3. Peut consulter uniquement ses propres coordonnées
4. Peut modifier uniquement ses propres informations

### Pour un administrateur `admin`
1. Se connecte via `login.html`
2. Accède au menu
3. Voit la liste complète des utilisateurs
4. Peut consulter les coordonnées de n'importe qui
5. Peut modifier les informations de n'importe qui
6. Navigation avec liens "Voir" et "Modifier" pour chaque utilisateur

## Avantages de l'Architecture

- **Code réutilisable** : Les mêmes servlets gèrent les deux rôles
- **Prise en compte immédiate** : Changement de rôle effectif dès la reconnexion
- **Sécurité** : Validation côté serveur des permissions
- **Maintenabilité** : Code centralisé et facile à maintenir

## Auteur

Aliocha Deflou

## Licence

Projet académique - BUT Informatique
