# 🚀 GUIDE DE SURVIE - CONTRÔLE TP WEB BACKEND
**Par Aliocha Deflou - BUT2 Info**

---

## ⚠️ INFOS IMPORTANTES DU PROF

### 🎯 Notation (TRÈS ENCOURAGEANTE !)
- ✅ **Ne fais pas la dernière question** → 18/20 (excellent !)
- ✅ **Ne fais pas les 2 dernières** → 15/20 (très bien !)
- 🎯 **Stratégie :** Vise la solidité plutôt que la vitesse !

### 📚 Pendant le contrôle
- ✅ **Tous documents autorisés** (cours, TP, tes fichiers)
- ✅ **Accès Moodle complet**
- ✅ **Nouveau contexte fourni** (ils te donnent un sujet/thème)
- ❌ **Pas de machine perso** (ordinateurs de l'IUT)
- 📖 **Cours concernés :** transp312 à transp316 (5 cours)

### 🔧 Points CRITIQUES à ne pas oublier

#### 1. CLASSPATH (IMPORTANT !)
Tu dois savoir compiler à la main. La commande complète :
```bash
javac -cp /chemin/vers/tomcat/lib/servlet-api.jar:. MaServlet.java
```

**OU plus simple si t'es dans le bon dossier :**
```bash
cd ~/tomcat/webapps/vide/WEB-INF/src/
javac MaServlet.java
# Ça marche si le CLASSPATH est déjà configuré
```

**Si erreur "package jakarta.servlet does not exist" :**
```bash
# Trouver où est servlet-api.jar
find ~/tomcat -name "servlet-api.jar"
# Puis compiler avec -cp
javac -cp /chemin/trouvé/servlet-api.jar:. MaServlet.java
```

#### 2. FERMER LES CONNEXIONS BDD (CRITIQUE !)
**❌ MAUVAIS (va te coûter des points) :**
```java
Connection cnx = DriverManager.getConnection(...);
Statement stmt = cnx.createStatement();
// Tu ne fermes rien !
```

**✅ BON (utilise TOUJOURS try-with-resources) :**
```java
try (Connection cnx = DriverManager.getConnection(...);
     PreparedStatement pstmt = cnx.prepareStatement(sql);
     ResultSet rs = pstmt.executeQuery()) {
    
    // Ton code ici
    
} // Tout se ferme automatiquement !
```

#### 3. DÉBUGGAGE (DEMANDÉ PAR LE PROF !)

**❌ JAMAIS faire ça :**
```java
try {
    // code
} catch (SQLException e) {
    // VIDE = TU PERDS DES POINTS !
}
```

**✅ TOUJOURS faire ça :**
```java
try {
    // Afficher le PreparedStatement AVANT de l'exécuter
    System.out.println("=== SQL: " + sql);
    System.out.println("=== Param1: " + param1);
    
    pstmt.executeUpdate();
    
} catch (SQLException e) {
    // OBLIGATOIRE : afficher l'erreur
    out.println("<div class='error'><p>" + e.getMessage() + "</p></div>");
    e.printStackTrace(); // Aussi dans les logs
}
```

---

## 📋 AVANT LE CONTRÔLE (PRÉPARATION)

### 1. Vérifier que Tomcat fonctionne
```bash
cd ~/tomcat/bin
./startup.sh
# Ouvrir navigateur : http://localhost:8080/
```

### 2. Préparer ton workspace
```bash
cd ~/tomcat/webapps/vide/
ls -la
# Tu dois voir : WEB-INF/
```

### 3. Mettre le CSS en place
```bash
cp style-tp.css ~/tomcat/webapps/vide/
```

### 4. Vérifier PostgreSQL
```bash
psql -U postgres -d ta_base
# Si ça marche, c'est bon ✅
```

### 5. Avoir tes fichiers sous la main
- ✅ SNIPPETS_COPIER_COLLER.txt (OUVERT dans un éditeur)
- ✅ style-tp.css (déjà copié)
- ✅ Ce guide

---

## ⚡ PENDANT LE CONTRÔLE - ORDRE DES OPÉRATIONS

### ÉTAPE 1 : Lire TOUT l'énoncé (5 min)
- 📖 Lis l'énoncé en entier AVANT de coder
- ✍️ Note sur papier ce qu'il faut faire
- 🎯 Identifie les servlets à créer

**Exemple de notes :**
```
Servlets à créer :
- servlet-Login (doPost)
- servlet-Liste (doGet)
- servlet-Ajouter (doGet + doPost)
- servlet-Supprimer (doPost)

Pages HTML :
- login.html
```

---

### ÉTAPE 2 : Créer/Vérifier la base de données (10 min)

#### A. Vérifier/créer les tables
```sql
-- Se connecter
psql -U postgres -d ta_base

-- Créer la table (exemple)
CREATE TABLE client (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50),
    prenom VARCHAR(50),
    age INTEGER
);

-- Table personne (pour l'auth)
CREATE TABLE personne (
    login VARCHAR(50) PRIMARY KEY,
    mdp VARCHAR(32),  -- MD5 = 32 caractères
    role VARCHAR(10),
    question VARCHAR(200),
    reponse VARCHAR(200)
);

-- Insérer un utilisateur de test (avec MD5)
INSERT INTO personne VALUES ('admin', MD5('admin'), 'admin', 'Question?', 'Reponse');
```

#### B. Vérifier config.prop
```bash
cd ~/tomcat/webapps/vide/WEB-INF/
cat config.prop
```

Doit contenir :
```properties
driver=org.postgresql.Driver
url=jdbc:postgresql://localhost:5432/ta_base
login=postgres
password=ton_mot_de_passe
```

---

### ÉTAPE 3 : Créer la structure de base (5 min)

#### A. Page HTML de login (si nécessaire)
```bash
cd ~/tomcat/webapps/vide/
nano login.html
```

Utilise le **snippet Bootstrap n°29** + un formulaire simple :
```html
<!DOCTYPE html>
<html lang='fr'>
<head>
    <meta charset='UTF-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1'>
    <title>Login</title>
    <link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css' rel='stylesheet'>
</head>
<body>
<div class='container'>
    <h1 class='mt-4'>Connexion</h1>
    <form method='POST' action='servlet-Login'>
        <div class='mb-3'>
            <label class='form-label'>Login</label>
            <input type='text' class='form-control' name='login' required>
        </div>
        <div class='mb-3'>
            <label class='form-label'>Mot de passe</label>
            <input type='password' class='form-control' name='mdp' required>
        </div>
        <button type='submit' class='btn btn-primary'>Se connecter</button>
    </form>
</div>
</body>
</html>
```

---

### ÉTAPE 4 : Créer tes servlets UNE PAR UNE (reste du temps)

#### 🎯 MÉTHODE OPTIMALE POUR CHAQUE SERVLET

##### 1. Créer le fichier Java
```bash
cd ~/tomcat/webapps/vide/WEB-INF/src/
nano ServletLogin.java
```

##### 2. Squelette de base (TOUJOURS pareil)
```java
import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Login")
public class ServletLogin extends HttpServlet {
    private String jdbcUrl, jdbcUser, jdbcPass, jdbcDriver;

    @Override
    public void init() throws ServletException {
        try {
            Properties p = new Properties();
            String configPath = getServletContext().getRealPath("/WEB-INF/config.prop");
            p.load(new FileInputStream(configPath));
            jdbcDriver = p.getProperty("driver");
            jdbcUrl = p.getProperty("url");
            jdbcUser = p.getProperty("login");
            jdbcPass = p.getProperty("password");
            Class.forName(jdbcDriver);
        } catch (Exception e) {
            throw new ServletException("Erreur init DB", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        // TON CODE ICI
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        // TON CODE ICI
    }
}
```

##### 3. Remplir avec tes snippets
Ouvre `SNIPPETS_COPIER_COLLER.txt` et copie-colle ce dont tu as besoin !

##### 4. Compiler
```bash
javac ServletLogin.java
mv ServletLogin.class ../classes/
```

##### 5. Tester IMMÉDIATEMENT
```
http://localhost:8080/vide/servlet-Login
```

**⚠️ NE PASSE PAS À LA SUIVANTE AVANT QUE ÇA MARCHE !**

---

## 🔧 COMPILATION ET DÉPLOIEMENT

### ⚠️ CLASSPATH - À VÉRIFIER EN PREMIER !

**Si tu as une erreur "package jakarta.servlet does not exist" :**

```bash
# Option 1 : Vérifier ton CLASSPATH
echo $CLASSPATH
# Doit contenir le chemin vers servlet-api.jar

# Option 2 : Compiler avec -cp explicite
javac -cp ~/tomcat/lib/servlet-api.jar:. MaServlet.java

# Option 3 : Configurer CLASSPATH une fois pour toutes
export CLASSPATH=~/tomcat/lib/servlet-api.jar:.
# Puis compile normalement
javac MaServlet.java
```

### Commandes essentielles
```bash
# Aller dans le dossier src
cd ~/tomcat/webapps/vide/WEB-INF/src/

# Compiler UNE servlet
javac MaServlet.java

# Déplacer le .class
mv MaServlet.class ../classes/

# Redémarrer Tomcat (si nécessaire)
cd ~/tomcat/bin/
./shutdown.sh
./startup.sh

# Voir les logs (si erreur)
cd ~/tomcat/logs/
tail -f catalina.out
```

### Compiler TOUTES tes servlets d'un coup
```bash
cd ~/tomcat/webapps/vide/WEB-INF/src/
javac *.java
mv *.class ../classes/
```

---

## 🐛 DÉBUGAGE - LES ERREURS CLASSIQUES

### Erreur 404 - Servlet not found
**Causes :**
- Mauvais nom dans `@WebServlet("/servlet-XXX")`
- Fichier .class pas dans `WEB-INF/classes/`
- Faute de frappe dans l'URL

**Solution :**
```bash
# Vérifier que le .class existe
ls ~/tomcat/webapps/vide/WEB-INF/classes/
# Doit afficher : ServletXXX.class
```

### Erreur 500 - Internal Server Error
**Causes :**
- Erreur dans ton code Java
- Problème de connexion DB
- Paramètre manquant

**Solution :**
```bash
# Regarder les logs
cd ~/tomcat/logs/
tail -30 catalina.out
# L'erreur est en rouge, lis la stacktrace
```

### Page blanche
**Causes :**
- Tu as oublié `res.setContentType("text/html;charset=UTF-8");`
- Exception silencieuse

**Solution :**
Ajoute au début de doGet/doPost :
```java
res.setContentType("text/html;charset=UTF-8");
PrintWriter out = res.getWriter();
```

### SQLException
**Causes :**
- Table n'existe pas
- Colonne mal orthographiée
- Problème dans la requête SQL

**Solution :**
1. Affiche l'erreur : `out.println("<p>" + e.getMessage() + "</p>");`
2. Teste ta requête SQL directement dans `psql`
3. Vérifie les noms de colonnes

---

## 📝 CHECKLIST AVANT DE RENDRE

### Pour chaque servlet
- [ ] Compilation OK (pas d'erreur)
- [ ] .class dans WEB-INF/classes/
- [ ] Accessible via navigateur
- [ ] Affiche quelque chose (pas de page blanche)
- [ ] Connexion DB fonctionne
- [ ] Les formulaires envoient bien les données
- [ ] Les messages d'erreur s'affichent

### Sécurité de base
- [ ] Utilise PreparedStatement (pas de concaténation SQL)
- [ ] Vérifie les sessions pour les pages protégées
- [ ] MD5 pour les mots de passe

### Rendu visuel
- [ ] Bootstrap inclus (si demandé)
- [ ] CSS style-tp.css inclus
- [ ] Pas de fautes d'orthographe dans les messages

---

## 💡 ASTUCES DE PRO

### 1. Commence par le plus simple
Ne commence PAS par l'authentification !
**Ordre recommandé :**
1. Servlet qui affiche une liste (SELECT simple)
2. Servlet qui ajoute (INSERT)
3. Servlet qui supprime (DELETE)
4. Login/Logout (auth)

### 2. Teste au fur et à mesure
**NE JAMAIS** coder 3 servlets sans tester !
Après chaque servlet : compile → teste → passe à la suivante

### 3. Copie-colle intelligemment
- Utilise SNIPPETS_COPIER_COLLER.txt
- Change les noms de variables
- Adapte les requêtes SQL

### 4. Gère ton temps
**Contrôle de 2h :**
- 10 min : Lire l'énoncé + BDD
- 80 min : Coder (20 min par servlet)
- 10 min : Relecture + tests finaux

**Si tu bloques sur quelque chose > 10 min → passe à la suite !**

### 5. Printf debugging
En cas de problème, ajoute des prints :
```java
System.out.println("=== DEBUG: Début doPost ===");
System.out.println("Login reçu : " + login);
System.out.println("Connexion DB OK");
```
Puis regarde dans `catalina.out`

### 6. Structure de code propre
Même sous pression, garde une structure claire :
```java
protected void doPost(...) {
    // 1. Récupérer paramètres
    String nom = req.getParameter("nom");
    
    // 2. Connexion DB
    try (Connection cnx = ...) {
        
        // 3. Requête SQL
        String sql = "...";
        
        // 4. Afficher résultat
        out.println("...");
        
    } catch (SQLException e) {
        // 5. Gestion erreur
        out.println("Erreur : " + e.getMessage());
    }
}
```

---

## 🎯 WORKFLOW OPTIMAL RÉSUMÉ

```
1. Lire énoncé → noter ce qu'il faut faire
2. Créer/vérifier BDD
3. Créer login.html
4. Pour chaque servlet :
   a. Créer fichier .java avec squelette
   b. Copier-coller snippets appropriés
   c. Adapter SQL et variables
   d. Compiler
   e. TESTER ✅
   f. Corriger erreurs
   g. RETESTER ✅
5. Vérifier toutes les fonctionnalités
6. Rendre
```

---

## 🔥 EN CAS DE PANIQUE

### Tu ne sais plus quoi faire ?
1. Respire 10 secondes
2. Relis l'énoncé
3. Note ce qui reste à faire
4. Prends la tâche la plus simple
5. Commence par un snippet

### Ton code ne compile pas ?
1. Lis l'erreur de compilation
2. Vérifie les imports
3. Vérifie les accolades `{}`
4. Vérifie les points-virgules `;`

### Rien ne s'affiche ?
```java
// Ajoute ça au tout début de doGet/doPost
res.setContentType("text/html;charset=UTF-8");
PrintWriter out = res.getWriter();
out.println("<h1>TEST - La servlet fonctionne !</h1>");
```
Si ça s'affiche → ton code marche, c'est juste la suite qui bug

### Tu as oublié comment faire X ?
**Ouvre SNIPPETS_COPIER_COLLER.txt et cherche (Ctrl+F) :**
- "SELECT" → afficher des données
- "INSERT" → ajouter
- "DELETE" → supprimer
- "LOGIN" → authentification
- "FORMULAIRE" → créer un form
- "BOOTSTRAP" → style

---

## 📚 RESSOURCES À AVOIR SOUS LA MAIN

### Fichiers essentiels ouverts
1. **SNIPPETS_COPIER_COLLER.txt** (le plus important !)
2. **Ce guide** (GUIDE_SURVIE_CONTROLE.md)
3. Un terminal ouvert sur `~/tomcat/webapps/vide/WEB-INF/src/`
4. Un navigateur sur `http://localhost:8080/vide/`

### Commandes à connaître par cœur
```bash
# Compiler + déployer
javac MaServlet.java && mv MaServlet.class ../classes/

# Redémarrer Tomcat
~/tomcat/bin/shutdown.sh && ~/tomcat/bin/startup.sh

# Voir les erreurs
tail -f ~/tomcat/logs/catalina.out
```

---

## ✅ TU ES PRÊT SI...

- [ ] Tomcat démarre sans erreur
- [ ] PostgreSQL est accessible
- [ ] config.prop est correct
- [ ] style-tp.css est dans webapps/vide/
- [ ] Tu sais compiler une servlet
- [ ] Tu as testé au moins une servlet de test
- [ ] Tu as SNIPPETS_COPIER_COLLER.txt ouvert
- [ ] Tu connais le workflow : créer → compiler → tester

---

## 🎓 DERNIERS CONSEILS

### DO ✅
- Teste après chaque servlet
- Utilise les snippets
- Garde ton code propre et indenté
- Affiche les erreurs SQL
- Respire et reste calme

### DON'T ❌
- Ne code pas 3 servlets sans tester
- Ne panic pas si ça marche pas du premier coup
- Ne perds pas de temps sur du design fancy
- N'oublie pas `res.setContentType()`
- Ne mets pas de mots de passe en clair (utilise MD5)

---

**BON COURAGE ! TU VAS ASSURER ! 💪🚀**

*Remember: Even Batman had a utility belt. Your snippets are your utility belt.*
