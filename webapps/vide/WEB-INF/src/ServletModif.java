import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Modif")
public class ServletModif extends HttpServlet {
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
        out.println("<title>Modifier les coordonnées</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 40px; }");
        out.println("label { display: block; margin-top: 10px; }");
        out.println("input { margin-bottom: 10px; width: 300px; }");
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

            // Si util : on ne peut modifier que ses propres coordonnées
            if ("util".equals(userRole)) {
                targetLogin = userLogin;
            }

            // Si admin sans paramètre : modifier ses propres coordonnées
            if ("admin".equals(userRole) && targetLogin == null) {
                targetLogin = userLogin;
            }

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                String sql = "SELECT nom, prenom, adresse, email, tel, datenaiss FROM personne WHERE login = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, targetLogin);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            out.println("<h1>Modification pour " + targetLogin + "</h1>");
                            out.println("<form method='POST' action='servlet-Modif'>");

                            // Champ caché pour conserver le login de l'utilisateur à modifier
                            out.println("<input type='hidden' name='targetLogin' value='" + targetLogin + "'>");

                            out.println("<label for='nom'>Nom :</label>");
                            out.println("<input type='text' id='nom' name='nom' value='" + rs.getString("nom") + "'><br>");

                            out.println("<label for='prenom'>Prénom :</label>");
                            out.println("<input type='text' id='prenom' name='prenom' value='" + rs.getString("prenom") + "'><br>");

                            out.println("<label for='adresse'>Adresse :</label>");
                            out.println("<input type='text' id='adresse' name='adresse' value='" + rs.getString("adresse") + "'><br>");

                            out.println("<label for='email'>Email :</label>");
                            out.println("<input type='email' id='email' name='email' value='" + rs.getString("email") + "'><br>");

                            out.println("<label for='tel'>Téléphone :</label>");
                            out.println("<input type='text' id='tel' name='tel' value='" + rs.getString("tel") + "'><br>");

                            out.println("<label for='datenaiss'>Date de naissance :</label>");
                            out.println("<input type='date' id='datenaiss' name='datenaiss' value='" + rs.getString("datenaiss") + "'><br>");

                            out.println("<br><input type='submit' value='Enregistrer'>");
                            out.println("</form>");
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

            if ("admin".equals(userRole)) {
                out.println("<br><p><a href='servlet-Lecture'>Retour à la liste</a></p>");
            }
            out.println("<p><a href='menu.html'>Retour au menu</a></p>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Modification</title>");
        out.println("<style>body { font-family: Arial; margin: 40px; }</style>");
        out.println("</head><body>");

        if (session == null || session.getAttribute("userLogin") == null) {
            out.println("<h1>Accès refusé</h1>");
            out.println("<p>Vous devez être authentifié pour accéder à cette page.</p>");
            out.println("<p><a href='login.html'>Se connecter</a></p>");
        } else {
            String userLogin = (String) session.getAttribute("userLogin");
            String userRole = (String) session.getAttribute("userRole");
            String targetLogin = request.getParameter("targetLogin");

            // Si util : on ne peut modifier que ses propres coordonnées
            if ("util".equals(userRole)) {
                targetLogin = userLogin;
            }

            // Si admin sans targetLogin : modifier ses propres coordonnées
            if ("admin".equals(userRole) && (targetLogin == null || targetLogin.isEmpty())) {
                targetLogin = userLogin;
            }

            String nom = request.getParameter("nom");
            String prenom = request.getParameter("prenom");
            String adresse = request.getParameter("adresse");
            String email = request.getParameter("email");
            String tel = request.getParameter("tel");
            String datenaiss = request.getParameter("datenaiss");

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                String sql = "UPDATE personne SET nom = ?, prenom = ?, adresse = ?, email = ?, tel = ?, datenaiss = ? WHERE login = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nom);
                    pstmt.setString(2, prenom);
                    pstmt.setString(3, adresse);
                    pstmt.setString(4, email);
                    pstmt.setString(5, tel);
                    pstmt.setDate(6, datenaiss != null && !datenaiss.isEmpty() ? Date.valueOf(datenaiss) : null);
                    pstmt.setString(7, targetLogin);

                    int rows = pstmt.executeUpdate();

                    if (rows > 0) {
                        out.println("<h1>Modification réussie</h1>");
                        out.println("<p>Les coordonnées de " + targetLogin + " ont été mises à jour.</p>");
                    } else {
                        out.println("<h1>Erreur</h1>");
                        out.println("<p>Aucune modification effectuée.</p>");
                    }
                }
            } catch (SQLException e) {
                out.println("<h1>Erreur</h1>");
                out.println("<p>Erreur lors de la mise à jour : " + e.getMessage() + "</p>");
                e.printStackTrace();
            }

            if ("admin".equals(userRole)) {
                out.println("<p><a href='servlet-Lecture?login=" + targetLogin + "'>Voir les coordonnées de " + targetLogin + "</a></p>");
                out.println("<p><a href='servlet-Lecture'>Retour à la liste</a></p>");
            } else {
                out.println("<p><a href='servlet-Lecture'>Voir mes coordonnées</a></p>");
            }
            out.println("<p><a href='menu.html'>Retour au menu</a></p>");
        }

        out.println("</body></html>");
    }
}
