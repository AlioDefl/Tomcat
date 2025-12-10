# Serveur Tomcat avec Application de Gestion des Utilisateurs

Serveur Apache Tomcat 11 complet avec une application web Java de gestion des utilisateurs incluant un système de rôles (admin/util).

## 🚀 Démarrage Rapide

```bash
# 1. Cloner le dépôt
git clone https://github.com/AlioDefl/Tomcat.git
cd Tomcat

# 2. Configurer la base de données (voir section Configuration)

# 3. Donner les permissions (Linux/Mac)
chmod +x bin/*.sh

# 4. Démarrer Tomcat
./bin/startup.sh

# 5. Accéder à l'application
# http://localhost:8080/vide/login.html
```

## 📦 Contenu du Dépôt

- **bin/** - Scripts de démarrage/arrêt (startup.sh, shutdown.sh)
- **lib/** - Bibliothèques JAR (servlet-api, postgresql, etc.)
- **conf/** - Configuration Tomcat
- **webapps/vide/** - Application de gestion des utilisateurs avec rôles

## 🔐 Application "vide" - Gestion des Utilisateurs

### Fonctionnalités

**Rôle util (Utilisateur):**
- ✅ Voir ses propres coordonnées
- ✅ Modifier ses propres informations
- ❌ Pas d'accès aux autres utilisateurs

**Rôle admin (Administrateur):**
- ✅ Liste complète des utilisateurs
- ✅ Voir/modifier n'importe quel utilisateur
- ✅ Navigation facilitée

### Comptes de Test

| Login | Mot de passe | Rôle |
|-------|--------------|------|
| aliocha | monmdp123 | admin |
| enzo | Azert59 | admin |
| testuser | password123 | util |
| emille | Maquillage59 | util |

## ⚙️ Installation et Configuration

### Prérequis
- Java 17+
- PostgreSQL
- Git

### Configuration Base de Données

1. **Créer la base:**
```sql
CREATE DATABASE votre_base;
```

2. **Créer la table:**
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

3. **Configurer la connexion:**

Éditez `webapps/vide/WEB-INF/config.prop`:
```properties
driver=org.postgresql.Driver
url=jdbc:postgresql://localhost:5432/votre_base
login=votre_login
password=votre_mot_de_passe
```

### Démarrer/Arrêter

**Linux/Mac:**
```bash
./bin/startup.sh    # Démarrer
./bin/shutdown.sh   # Arrêter
```

**Windows:**
```bash
bin\startup.bat     # Démarrer
bin\shutdown.bat    # Arrêter
```

## 🔧 Gestion des Rôles

Changer le rôle d'un utilisateur dans PostgreSQL:
```sql
-- Promouvoir en admin
UPDATE personne SET role = 'admin' WHERE login = 'nom_utilisateur';

-- Rétrograder en util
UPDATE personne SET role = 'util' WHERE login = 'nom_utilisateur';
```

Le changement est effectif immédiatement après reconnexion.

## 🛠️ Développement

### Compiler les Servlets
```bash
cd webapps/vide/WEB-INF/src
javac -cp "../../../../lib/servlet-api.jar:." *.java -d ../classes
```

### Consulter les Logs
```bash
tail -f logs/catalina.out
```

## 🏗️ Architecture

**Technologies:**
- Java 17 avec Jakarta Servlet API
- PostgreSQL avec JDBC
- Apache Tomcat 11.0.13
- HTML5/CSS3

**Servlets:**
- `Authent.java` - Authentification
- `ServletLecture.java` - Consultation avec gestion des rôles
- `ServletModif.java` - Modification avec gestion des rôles
- `ServletMenu.java` - Menu dynamique
- `ServletDeconnecte.java` - Déconnexion

**Sécurité:**
- PreparedStatement (anti-injection SQL)
- Vérification systématique des sessions
- Contrôle d'accès basé sur les rôles

## 🐛 Dépannage

**Le serveur ne démarre pas:**
- Vérifiez Java 17+ : `java -version`
- Vérifiez que le port 8080 est libre
- Consultez `logs/catalina.out`

**Problème de connexion BDD:**
- Vérifiez PostgreSQL démarré
- Vérifiez `webapps/vide/WEB-INF/config.prop`
- Vérifiez driver dans `lib/postgresql-*.jar`

**Page 404:**
- URL correcte : `http://localhost:8080/vide/login.html`
- Vérifiez les logs de déploiement

## 📝 Licence

Projet académique - BUT Informatique

## 👤 Auteur

Aliocha Deflou

**Repository:** https://github.com/AlioDefl/Tomcat.git
