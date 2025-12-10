import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Inscription")
public class ServletInscription extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String login = request.getParameter("login");
        String mdp = request.getParameter("mdp");
        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");
        String adresse = request.getParameter("adresse");
        String email = request.getParameter("email");
        String tel = request.getParameter("tel");
        String datenaiss = request.getParameter("datenaiss");
        String questionSecrete = request.getParameter("question_secrete");
        String reponseSecrete = request.getParameter("reponse_secrete");

        out.println("<!doctype html>");
        out.println("<html lang='fr'><head><meta charset='utf-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<title>Inscription</title>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body>");
        out.println("<div class='container mt-5'>");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            String checkSql = "SELECT COUNT(*) FROM personne WHERE login = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, login);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        out.println("<h1>Erreur</h1>");
                        out.println("<p>Ce login est déjà utilisé. Veuillez en choisir un autre.</p>");
                        out.println("<p><a href='inscription.html'>Retour à l'inscription</a></p>");
                        out.println("<p><a href='login.html'>Retour à la page de connexion</a></p>");
                        out.println("</body></html>");
                        return;
                    }
                }
            }

            // Insérer le nouvel utilisateur avec le rôle 'util' et le mot de passe haché en MD5
            String insertSql = "INSERT INTO personne (login, mdp, nom, prenom, adresse, email, tel, datenaiss, role, question_secrete, reponse_secrete) " +
                             "VALUES (?, MD5(?), ?, ?, ?, ?, ?, ?, 'util', ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, login);
                pstmt.setString(2, mdp);
                pstmt.setString(3, nom);
                pstmt.setString(4, prenom);
                pstmt.setString(5, adresse);
                pstmt.setString(6, email);
                pstmt.setString(7, tel);
                pstmt.setDate(8, datenaiss != null && !datenaiss.isEmpty() ? Date.valueOf(datenaiss) : null);
                pstmt.setString(9, questionSecrete);
                pstmt.setString(10, reponseSecrete);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    out.println("<h1>Inscription réussie !</h1>");
                    out.println("<p>Votre compte a été créé avec succès.</p>");
                    out.println("<p>Login : <strong>" + login + "</strong></p>");
                    out.println("<p>Rôle : <strong>Utilisateur (util)</strong></p>");
                    out.println("<br>");
                    out.println("<p><a href='login.html'>Se connecter</a></p>");
                } else {
                    out.println("<h1>Erreur</h1>");
                    out.println("<p>L'inscription a échoué. Veuillez réessayer.</p>");
                    out.println("<p><a href='inscription.html'>Retour à l'inscription</a></p>");
                }
            }
        } catch (SQLException e) {
            out.println("<h1>Erreur</h1>");
            out.println("<p>Erreur lors de l'inscription : " + e.getMessage() + "</p>");
            out.println("<p><a href='inscription.html'>Retour à l'inscription</a></p>");
            e.printStackTrace();
        }

        out.println("</body></html>");
    }
}
