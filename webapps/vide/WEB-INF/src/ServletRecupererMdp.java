import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-RecupererMdp")
public class ServletRecupererMdp extends HttpServlet {
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

        String login = request.getParameter("login");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Récupération de mot de passe</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 40px; }");
        out.println("label { display: block; margin-top: 10px; }");
        out.println("input { margin-bottom: 10px; width: 300px; padding: 5px; }");
        out.println("</style>");
        out.println("</head><body>");

        if (login == null || login.isEmpty()) {
            out.println("<h1>Erreur</h1>");
            out.println("<p>Aucun login fourni.</p>");
            out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
            out.println("</body></html>");
            return;
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            String sql = "SELECT question_secrete FROM personne WHERE login = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, login);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String questionSecrete = rs.getString("question_secrete");

                        if (questionSecrete == null || questionSecrete.isEmpty()) {
                            out.println("<h1>Erreur</h1>");
                            out.println("<p>Aucune question secrète n'a été définie pour ce compte.</p>");
                            out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
                        } else {
                            out.println("<h1>Récupération de mot de passe</h1>");
                            out.println("<p>Login : <strong>" + login + "</strong></p>");
                            out.println("<p>Question secrète : <strong>" + questionSecrete + "</strong></p>");

                            out.println("<form method='POST' action='servlet-RecupererMdp'>");
                            out.println("<input type='hidden' name='login' value='" + login + "'>");

                            out.println("<label for='reponse'>Réponse :</label>");
                            out.println("<input type='text' id='reponse' name='reponse' required>");

                            out.println("<br><input type='submit' value='Récupérer mon mot de passe'>");
                            out.println("</form>");

                            out.println("<br><p><a href='recuperer-mdp.html'>Retour</a></p>");
                        }
                    } else {
                        out.println("<h1>Erreur</h1>");
                        out.println("<p>Ce login n'existe pas.</p>");
                        out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
                    }
                }
            }
        } catch (SQLException e) {
            out.println("<h1>Erreur</h1>");
            out.println("<p>Erreur lors de la récupération : " + e.getMessage() + "</p>");
            out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
            e.printStackTrace();
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String login = request.getParameter("login");
        String reponse = request.getParameter("reponse");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Récupération de mot de passe</title>");
        out.println("<style>body { font-family: Arial; margin: 40px; }</style>");
        out.println("</head><body>");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            String sql = "SELECT mdp, reponse_secrete FROM personne WHERE login = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, login);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String reponseSecrete = rs.getString("reponse_secrete");
                        String mdp = rs.getString("mdp");

                        if (reponse.equalsIgnoreCase(reponseSecrete)) {
                            out.println("<h1>Récupération réussie !</h1>");
                            out.println("<p>Votre mot de passe est : <strong>" + mdp + "</strong></p>");
                            out.println("<br>");
                            out.println("<p><a href='login.html'>Se connecter</a></p>");
                        } else {
                            out.println("<h1>Erreur</h1>");
                            out.println("<p>La réponse est incorrecte.</p>");
                            out.println("<p><a href='servlet-RecupererMdp?login=" + login + "'>Réessayer</a></p>");
                            out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
                        }
                    } else {
                        out.println("<h1>Erreur</h1>");
                        out.println("<p>Utilisateur non trouvé.</p>");
                        out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
                    }
                }
            }
        } catch (SQLException e) {
            out.println("<h1>Erreur</h1>");
            out.println("<p>Erreur lors de la vérification : " + e.getMessage() + "</p>");
            out.println("<p><a href='recuperer-mdp.html'>Retour</a></p>");
            e.printStackTrace();
        }

        out.println("</body></html>");
    }
}
