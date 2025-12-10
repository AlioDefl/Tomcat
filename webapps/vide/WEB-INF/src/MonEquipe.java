import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-MonEquipe")
public class MonEquipe extends HttpServlet {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        String equipeSelectionnee = req.getParameter("equipe");

        if (equipeSelectionnee != null && !equipeSelectionnee.isEmpty()) {
            Cookie cookieEquipe = new Cookie("equipePreferee", equipeSelectionnee);
            cookieEquipe.setMaxAge(60 * 60);
            res.addCookie(cookieEquipe);
        } else {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for(int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("equipePreferee")) {
                        equipeSelectionnee = cookies[i].getValue();
                        break;
                    }
                }
            }
        }

        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        out.println("<title>Mon Équipe Préférée</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; text-align: center; }");
        out.println("form { margin: 30px 0; }");
        out.println("label { display: block; margin-bottom: 10px; font-weight: bold; color: #555; }");
        out.println("select { width: 100%; padding: 12px; font-size: 16px; border: 2px solid #ddd; border-radius: 4px; margin-bottom: 20px; }");
        out.println("select:focus { border-color: #4CAF50; outline: none; }");
        out.println("button { width: 100%; padding: 12px; font-size: 16px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; }");
        out.println("button:hover { background-color: #45a049; }");
        out.println(".result { background-color: #e7f3fe; border-left: 6px solid #2196F3; padding: 20px; margin: 20px 0; border-radius: 4px; }");
        out.println(".result h2 { margin-top: 0; color: #2196F3; }");
        out.println(".error { color: red; border: 2px solid red; padding: 15px; margin: 20px 0; border-radius: 4px; }");
        out.println("a.link-rencontres { display: block; text-align: center; margin: 20px 0; padding: 12px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 4px; }");
        out.println("a.link-rencontres:hover { background-color: #0b7dda; }");
        out.println("</style></head><body>");
        out.println("<div class='container'>");
        out.println("<h1>Mon Équipe Préférée</h1>");

        try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            if (equipeSelectionnee != null && !equipeSelectionnee.isEmpty()) {
                afficherEquipeChoisie(out, cnx, equipeSelectionnee);
            }

            out.println("<form method='get' action='servlet-MonEquipe'>");
            out.println("<label for='equipe'>Choisissez votre équipe préférée :</label>");
            out.println("<select name='equipe' id='equipe' required>");
            out.println("<option value=''>-- Sélectionnez une équipe --</option>");

            String sql = "SELECT num_equipe, nom_equipe FROM equipes ORDER BY nom_equipe";
            try (Statement stmt = cnx.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    int numEquipe = rs.getInt("num_equipe");
                    String nomEquipe = rs.getString("nom_equipe");
                    String selected = String.valueOf(numEquipe).equals(equipeSelectionnee) ? " selected" : "";
                    out.println("<option value='" + numEquipe + "'" + selected + ">" +
                               escape(nomEquipe) + "</option>");
                }
            }

            out.println("</select>");
            out.println("<button type='submit'>Valider mon choix</button>");
            out.println("</form>");

        } catch (SQLException ex) {
            out.println("<div class='error'>");
            out.println("<h2>Erreur de connexion/SQL:</h2>");
            out.println("<p>" + escape(ex.getMessage()) + "</p>");
            out.println("<p>Configuration actuelle :</p>");
            out.println("<ul>");
            out.println("<li>URL: " + escape(jdbcUrl) + "</li>");
            out.println("<li>User: " + escape(jdbcUser) + "</li>");
            out.println("<li>Driver: " + escape(jdbcDriver) + "</li>");
            out.println("</ul>");
            out.println("</div>");
            ex.printStackTrace();
        }

        out.println("</div>");
        out.println("</body></html>");
    }

    private void afficherEquipeChoisie(PrintWriter out, Connection cnx, String numEquipeStr) {
        try {
            int numEquipe = Integer.parseInt(numEquipeStr);
            String sql = "SELECT nom_equipe FROM equipes WHERE num_equipe = ?";

            try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
                pstmt.setInt(1, numEquipe);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String nomEquipe = rs.getString("nom_equipe");
                        out.println("<div class='result'>");
                        out.println("<h2>Votre équipe préférée :</h2>");
                        out.println("<p style='font-size: 24px; text-align: center; margin: 20px 0;'><strong>" +
                                   escape(nomEquipe) + "</strong></p>");
                        out.println("<a href='servlet-ListeRencontresEvol' class='link-rencontres'>Voir les rencontres de mon équipe</a>");
                        out.println("</div>");
                    }
                }
            }
        } catch (NumberFormatException | SQLException e) {
            out.println("<div class='error'>");
            out.println("<p>Erreur lors de la récupération de l'équipe.</p>");
            out.println("</div>");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
