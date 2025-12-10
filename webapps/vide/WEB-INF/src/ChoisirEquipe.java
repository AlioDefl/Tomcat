import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-ChoisirEquipe")
public class ChoisirEquipe extends HttpServlet {
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
            throw new ServletException("Driver JDBC introuvable : " + jdbcDriver, e);
        } catch (IOException e) {
            throw new ServletException("Échec du chargement de config.prop : " + e.getMessage(), e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String equipe = request.getParameter("equipe");

        // Si une équipe est choisie, créer le cookie et rediriger
        if (equipe != null && !equipe.isEmpty()) {
            Cookie cookie = new Cookie("equipeFavorite", equipe);
            cookie.setMaxAge(60 * 60 * 24 * 30); // 30 jours
            response.addCookie(cookie);
            response.sendRedirect("servlet-JoueursDisponibles");
            return;
        }

        // Sinon, afficher le formulaire de choix
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Choisir une équipe</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; max-width: 600px; margin: 20px auto; }");
        out.println("h1 { text-align: center; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Choisir votre équipe favorite</h1>");

        try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {

            String sql = "SELECT num_equipe, nom_equipe FROM equipes ORDER BY nom_equipe";

            try (PreparedStatement pstmt = cnx.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                out.println("<ul>");
                while (rs.next()) {
                    int numEquipe = rs.getInt("num_equipe");
                    String nomEquipe = rs.getString("nom_equipe");
                    out.println("<li><a href='servlet-ChoisirEquipe?equipe=" + numEquipe + "'>" + nomEquipe + "</a></li>");
                }
                out.println("</ul>");
            }

        } catch (SQLException ex) {
            out.println("<p>Erreur : " + ex.getMessage() + "</p>");
        }

        out.println("</body>");
        out.println("</html>");
    }
}
