import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Lecture")
public class ServletLecture extends HttpServlet {
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPass;
    private String jdbcDriver;

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
        } catch (ClassNotFoundException e) {
            throw new ServletException("driver JDBC introuvable : " + jdbcDriver, e);
        } catch (IOException e) {
            throw new ServletException("échec du chargement de config.prop : " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Coordonnées</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 40px; }");
        out.println("table { border-collapse: collapse; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println("</style>");
        out.println("</head><body>");

        if (session == null || session.getAttribute("userLogin") == null) {
            out.println("<h1>Accès refusé</h1>");
            out.println("<p>Vous devez être authentifié pour accéder à cette page.</p>");
            out.println("<p><a href='login.html'>Se connecter</a></p>");
        } else {
            String userLogin = (String) session.getAttribute("userLogin");
            String userRole = (String) session.getAttribute("userRole");
            String targetLogin = request.getParameter("login");

            // Si util : on ne peut voir que ses propres coordonnées
            if ("util".equals(userRole)) {
                targetLogin = userLogin;
            }

            // Si admin sans paramètre login : afficher la liste de tous les utilisateurs
            if ("admin".equals(userRole) && targetLogin == null) {
                out.println("<h1>Liste des utilisateurs</h1>");
                out.println("<p>Vous êtes connecté en tant qu'administrateur : " + userLogin + "</p>");

                try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                    String sql = "SELECT login, nom, prenom, role FROM personne ORDER BY login";

                    try (PreparedStatement pstmt = conn.prepareStatement(sql);
                         ResultSet rs = pstmt.executeQuery()) {

                        out.println("<table>");
                        out.println("<tr><th>Login</th><th>Nom</th><th>Prénom</th><th>Rôle</th><th>Actions</th></tr>");

                        while (rs.next()) {
                            String login = rs.getString("login");
                            String nom = rs.getString("nom");
                            String prenom = rs.getString("prenom");
                            String role = rs.getString("role");

                            out.println("<tr>");
                            out.println("<td>" + login + "</td>");
                            out.println("<td>" + (nom != null ? nom : "") + "</td>");
                            out.println("<td>" + (prenom != null ? prenom : "") + "</td>");
                            out.println("<td>" + (role != null ? role : "") + "</td>");
                            out.println("<td><a href='servlet-Lecture?login=" + login + "'>Voir</a> | ");
                            out.println("<a href='servlet-Modif?login=" + login + "'>Modifier</a></td>");
                            out.println("</tr>");
                        }

                        out.println("</table>");
                    }
                } catch (SQLException e) {
                    out.println("<h1>Erreur</h1>");
                    out.println("<p>Erreur lors de la récupération des données : " + e.getMessage() + "</p>");
                    e.printStackTrace();
                }

                out.println("<br><p><a href='menu.html'>Retour au menu</a></p>");

            } else {
                // Afficher les coordonnées d'un utilisateur spécifique
                try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                    String sql = "SELECT nom, prenom, adresse, email, tel, datenaiss, role FROM personne WHERE login = ?";

                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, targetLogin);

                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                out.println("<h1>Coordonnées de " + targetLogin + "</h1>");
                                out.println("<table>");
                                out.println("<tr><th>Login</th><td>" + targetLogin + "</td></tr>");
                                out.println("<tr><th>Nom</th><td>" + rs.getString("nom") + "</td></tr>");
                                out.println("<tr><th>Prénom</th><td>" + rs.getString("prenom") + "</td></tr>");
                                out.println("<tr><th>Adresse</th><td>" + rs.getString("adresse") + "</td></tr>");
                                out.println("<tr><th>Email</th><td>" + rs.getString("email") + "</td></tr>");
                                out.println("<tr><th>Téléphone</th><td>" + rs.getString("tel") + "</td></tr>");
                                out.println("<tr><th>Date de naissance</th><td>" + rs.getString("datenaiss") + "</td></tr>");
                                out.println("<tr><th>Rôle</th><td>" + rs.getString("role") + "</td></tr>");
                                out.println("</table>");

                                if ("admin".equals(userRole)) {
                                    out.println("<p><a href='servlet-Modif?login=" + targetLogin + "'>Modifier cet utilisateur</a></p>");
                                    out.println("<p><a href='servlet-Lecture'>Retour à la liste</a></p>");
                                }
                            } else {
                                out.println("<h1>Erreur</h1>");
                                out.println("<p>Aucune information trouvée pour cet utilisateur.</p>");
                            }
                        }
                    }
                } catch (SQLException e) {
                    out.println("<h1>Erreur</h1>");
                    out.println("<p>Erreur lors de la récupération des données : " + e.getMessage() + "</p>");
                    e.printStackTrace();
                }

                out.println("<br><p><a href='menu.html'>Retour au menu</a></p>");
            }
        }

        out.println("</body></html>");
    }
}
