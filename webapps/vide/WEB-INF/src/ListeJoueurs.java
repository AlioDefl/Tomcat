import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-ListeJoueurs")
public class ListeJoueurs extends HttpServlet {
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
    protected void service(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        String mode = req.getParameter("mode");
        
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        out.println("<title>Liste des joueurs</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
        out.println("tr:hover { background-color: #ddd; }");
        out.println(".red { color: red; font-weight: bold; }");
        out.println("h2 { color: #333; margin-top: 30px; }");
        out.println("</style></head><body>");
        out.println("<h1>Liste des joueurs</h1>");
        out.println("<p><a href='?'>Vue simple</a> · <a href='?mode=clubs'>Vue par club</a></p>");

        try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            if ("clubs".equalsIgnoreCase(mode)) {
                renderByClub(out, cnx);
            } else {
                renderFlat(out, cnx);
            }
        } catch (SQLException ex) {
            out.println("<div style='color: red; border: 2px solid red; padding: 10px; margin: 20px 0;'>");
            out.println("<h2>Erreur de connexion/SQL:</h2>");
            out.println("<p>" + escape(ex.getMessage()) + "</p>");
            out.println("<p>Configuration actuelle :</p>");
            out.println("<ul>");
            out.println("<li>URL: " + escape(jdbcUrl) + "</li>");
            out.println("<li>User: " + escape(jdbcUser) + "</li>");
            out.println("<li>Driver: " + escape(jdbcDriver) + "</li>");
            out.println("</ul>");
            out.println("<p>Vérifiez le driver dans WEB-INF/lib, et les variables JDBC_URL/JDBC_USER/JDBC_PASS.</p>");
            out.println("</div>");
            ex.printStackTrace();
        }
        
        out.println("</body></html>");
    }

    private void renderFlat(PrintWriter out, Connection cnx) throws SQLException {
        String sql = "SELECT j.nom_joueur, j.pays, j.date_naissance, j.poste, e.nom_equipe " +
                     "FROM joueurs j " +
                     "LEFT JOIN equipes e ON j.club = e.num_equipe " +
                     "ORDER BY j.nom_joueur";
        
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            out.println("<table>");
            out.println("<thead><tr>");
            out.println("<th>Nom</th><th>Pays</th><th>Date naissance</th><th>Poste</th><th>Club</th>");
            out.println("</tr></thead><tbody>");
            
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                String nom = rs.getString("nom_joueur");
                String pays = rs.getString("pays");
                Date dateNaiss = rs.getDate("date_naissance");
                String poste = rs.getString("poste");
                String club = rs.getString("nom_equipe"); 
                
                boolean plusDe30 = false;
                if (dateNaiss != null) {
                    LocalDate naissance = dateNaiss.toLocalDate();
                    long age = ChronoUnit.YEARS.between(naissance, today);
                    plusDe30 = (age > 30);
                }
                
                String rowClass = plusDe30 ? " class='red'" : "";
                out.println("<tr" + rowClass + ">");
                out.println("<td>" + escape(nom) + "</td>");
                out.println("<td>" + escape(pays) + "</td>");
                out.println("<td>" + escape(dateNaiss != null ? dateNaiss.toString() : "") + "</td>");
                out.println("<td>" + escape(poste) + "</td>");
                out.println("<td>" + escape(club) + "</td>");
                out.println("</tr>");
            }
            out.println("</tbody></table>");
        }
    }
    

    private void renderByClub(PrintWriter out, Connection cnx) throws SQLException {
        String sqlClubs = "SELECT num_equipe, nom_equipe FROM equipes ORDER BY nom_equipe";
        
        try (Statement stmt = cnx.createStatement();
             ResultSet rsClubs = stmt.executeQuery(sqlClubs)) {
            
            LocalDate today = LocalDate.now();
            
            while (rsClubs.next()) {
                int numEquipe = rsClubs.getInt("num_equipe");
                String nomEquipe = rsClubs.getString("nom_equipe");
                
                out.println("<h2>" + escape(nomEquipe) + "</h2>");
                
                String sqlJoueurs = "SELECT nom_joueur, pays, date_naissance, poste " +
                                   "FROM joueurs WHERE club = ? ORDER BY nom_joueur";
                
                try (PreparedStatement pstmt = cnx.prepareStatement(sqlJoueurs)) {
                    pstmt.setInt(1, numEquipe); // Club est un INTEGER
                    
                    try (ResultSet rsJoueurs = pstmt.executeQuery()) {
                        out.println("<table>");
                        out.println("<thead><tr>");
                        out.println("<th>Nom</th><th>Pays</th><th>Date naissance</th><th>Poste</th>");
                        out.println("</tr></thead><tbody>");
                        
                        while (rsJoueurs.next()) {
                            String nom = rsJoueurs.getString("nom_joueur");
                            String pays = rsJoueurs.getString("pays");
                            Date dateNaiss = rsJoueurs.getDate("date_naissance");
                            String poste = rsJoueurs.getString("poste");
                            
                            boolean plusDe30 = false;
                            if (dateNaiss != null) {
                                LocalDate naissance = dateNaiss.toLocalDate();
                                long age = ChronoUnit.YEARS.between(naissance, today);
                                plusDe30 = (age > 30);
                            }
                            
                            String rowClass = plusDe30 ? " class='red'" : "";
                            out.println("<tr" + rowClass + ">");
                            out.println("<td>" + escape(nom) + "</td>");
                            out.println("<td>" + escape(pays) + "</td>");
                            out.println("<td>" + escape(dateNaiss != null ? dateNaiss.toString() : "") + "</td>");
                            out.println("<td>" + escape(poste) + "</td>");
                            out.println("</tr>");
                        }
                        out.println("</tbody></table>");
                    }
                }
            }
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
