import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Login")
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Login</title>");
        out.println("<style>body { font-family: Arial; margin: 40px; }</style>");
        out.println("</head><body>");

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

