import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-CreerRencontre")
public class CreerRencontre extends HttpServlet {
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
        out.println("<title>Créer une rencontre</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("form { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }");
        out.println("label { display: block; margin-top: 15px; font-weight: bold; }");
        out.println("input[type=text], input[type=number], input[type=date] { width: 100%; padding: 8px; margin-top: 5px; border: 1px solid #ddd; border-radius: 3px; box-sizing: border-box; }");
        out.println("input[type=submit] { margin-top: 20px; padding: 10px 20px; background-color: #4CAF50; color: white; border: none; border-radius: 3px; cursor: pointer; font-size: 16px; }");
        out.println("input[type=submit]:hover { background-color: #45a049; }");
        out.println("h1 { text-align: center; color: #333; }");
        out.println(".info { background-color: #e7f3fe; border-left: 4px solid #2196F3; padding: 10px; margin: 20px 0; }");
        out.println("</style></head><body>");
        
        out.println("<h1>Créer une nouvelle rencontre</h1>");
        
        out.println("<form method='POST' action='servlet-CreerRencontre'>");
        
        out.println("<label for='num_match'>Numéro du match :</label>");
        out.println("<input type='number' id='num_match' name='num_match' required>");
        
        out.println("<label for='eq1'>Équipe 1 (domicile) - ID :</label>");
        out.println("<input type='number' id='eq1' name='eq1' required>");
        
        out.println("<label for='eq2'>Équipe 2 (extérieur) - ID :</label>");
        out.println("<input type='number' id='eq2' name='eq2' required>");
        
        out.println("<label for='jour'>Date du match :</label>");
        out.println("<input type='date' id='jour' name='jour' required>");
        
        out.println("<label for='sc1'>Score Équipe 1 :</label>");
        out.println("<input type='number' id='sc1' name='sc1' min='0' required>");
        
        out.println("<label for='sc2'>Score Équipe 2 :</label>");
        out.println("<input type='number' id='sc2' name='sc2' min='0' required>");
        
        out.println("<input type='submit' value='Créer la rencontre'>");
        out.println("</form>");
        
        out.println("<div class='info'>");
        out.println("<p><strong>Note :</strong> Pour obtenir les IDs des équipes, consultez la table 'equipes' de votre base de données.</p>");
        out.println("</div>");
        
        out.println("<p style='text-align: center;'><a href='servlet-ListeRencontres'>Retour à la liste des rencontres</a></p>");
        
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        
        try {
            int numMatch = Integer.parseInt(req.getParameter("num_match"));
            int eq1 = Integer.parseInt(req.getParameter("eq1"));
            int eq2 = Integer.parseInt(req.getParameter("eq2"));
            String jour = req.getParameter("jour");
            int sc1 = Integer.parseInt(req.getParameter("sc1"));
            int sc2 = Integer.parseInt(req.getParameter("sc2"));
            
            try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                String sql = "INSERT INTO rencontres (num_match, eq1, eq2, jour, sc1, sc2) VALUES (?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
                    pstmt.setInt(1, numMatch);
                    pstmt.setInt(2, eq1);
                    pstmt.setInt(3, eq2);
                    pstmt.setDate(4, Date.valueOf(jour));
                    pstmt.setInt(5, sc1);
                    pstmt.setInt(6, sc2);
                    
                    int rowsInserted = pstmt.executeUpdate();
                    
                    if (rowsInserted > 0) {
                        res.sendRedirect("servlet-ListeRencontres");
                    } else {
                        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
                        out.println("<title>Erreur</title></head><body>");
                        out.println("<h1>Erreur lors de l'insertion</h1>");
                        out.println("<p>Aucune ligne n'a été insérée.</p>");
                        out.println("<p><a href='servlet-CreerRencontre'>Retour au formulaire</a></p>");
                        out.println("</body></html>");
                    }
                }
            } catch (SQLException ex) {
                out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
                out.println("<title>Erreur SQL</title>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
                out.println(".error { color: red; border: 2px solid red; padding: 10px; margin: 20px 0; }");
                out.println("</style></head><body>");
                out.println("<h1>Erreur lors de l'insertion</h1>");
                out.println("<div class='error'>");
                out.println("<h2>Erreur SQL :</h2>");
                out.println("<p>" + escape(ex.getMessage()) + "</p>");
                out.println("</div>");
                out.println("<p><strong>Suggestions :</strong></p>");
                out.println("<ul>");
                out.println("<li>Vérifiez que le numéro de match n'existe pas déjà</li>");
                out.println("<li>Vérifiez que les IDs des équipes existent dans la table 'equipes'</li>");
                out.println("<li>Vérifiez le format de la date</li>");
                out.println("</ul>");
                out.println("<p><a href='servlet-CreerRencontre'>Retour au formulaire</a></p>");
                out.println("</body></html>");
                ex.printStackTrace();
            }
        } catch (NumberFormatException ex) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            out.println("<title>Erreur</title></head><body>");
            out.println("<h1>Erreur de format</h1>");
            out.println("<p>Les valeurs numériques saisies ne sont pas valides.</p>");
            out.println("<p><a href='servlet-CreerRencontre'>Retour au formulaire</a></p>");
            out.println("</body></html>");
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

