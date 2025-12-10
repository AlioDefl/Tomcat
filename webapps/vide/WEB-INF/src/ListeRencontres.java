import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-ListeRencontres")
public class ListeRencontres extends HttpServlet {
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
        
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        out.println("<title>Liste des rencontres</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
        out.println("tr:hover { background-color: #ddd; }");
        out.println("h2 { color: #333; margin-top: 30px; }");
        out.println("</style></head><body>");
        out.println("<h1>Liste des rencontres</h1>");

        try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            String sql = "SELECT num_match, eq1, eq2, jour, sc1, sc2 " +
                         "FROM rencontres " +
                         "ORDER BY jour DESC, num_match";
            
            try (Statement stmt = cnx.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                out.println("<table>");
                out.println("<thead><tr>");
                out.println("<th>Numéro match</th><th>Équipe 1 (domicile)</th><th>Équipe 2 (extérieur)</th>");
                out.println("<th>Date</th><th>Score Eq1</th><th>Score Eq2</th>");
                out.println("</tr></thead><tbody>");
                
                while (rs.next()) {
                    int numMatch = rs.getInt("num_match");
                    int eq1 = rs.getInt("eq1");
                    int eq2 = rs.getInt("eq2");
                    Date jour = rs.getDate("jour");
                    int sc1 = rs.getInt("sc1");
                    int sc2 = rs.getInt("sc2");
                    
                    out.println("<tr>");
                    out.println("<td>" + numMatch + "</td>");
                    out.println("<td>" + eq1 + "</td>");
                    out.println("<td>" + eq2 + "</td>");
                    out.println("<td>" + (jour != null ? jour.toString() : "") + "</td>");
                    out.println("<td>" + sc1 + "</td>");
                    out.println("<td>" + sc2 + "</td>");
                    out.println("</tr>");
                }
                out.println("</tbody></table>");
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
            out.println("<p>Vérifiez le driver dans WEB-INF/lib, et les variables de connexion.</p>");
            out.println("</div>");
            ex.printStackTrace();
        }
        
        out.println("</body></html>");
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

