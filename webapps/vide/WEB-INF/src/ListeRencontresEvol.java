import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-ListeRencontresEvol")
public class ListeRencontresEvol extends HttpServlet {
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPass;
    private String jdbcDriver;

    private static final List<String> COLONNES_AUTORISEES = Arrays.asList(
        "num_match", "eq1", "eq2", "jour", "sc1", "sc2"
    );

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

        String triParam = req.getParameter("tri");
        String sensParam = req.getParameter("sens");

        String equipePreferee = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for(int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("equipePreferee")) {
                    equipePreferee = cookies[i].getValue();
                    break;
                }
            }
        }

        String orderBy = "jour DESC, num_match";
        String colonneActuelle = null;
        String sensActuel = "desc";

        if (triParam != null && !triParam.trim().isEmpty()) {
            if (COLONNES_AUTORISEES.contains(triParam.trim())) {
                colonneActuelle = triParam.trim();

                if (sensParam != null && sensParam.trim().equalsIgnoreCase("desc")) {
                    sensActuel = "desc";
                } else {
                    sensActuel = "asc";
                }

                orderBy = colonneActuelle + " " + sensActuel.toUpperCase();
            } else {
                out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
                out.println("<title>Erreur</title></head><body>");
                out.println("<h1 style='color: red;'>Erreur : Colonne de tri invalide</h1>");
                out.println("<p>Les colonnes autorisées sont : " + COLONNES_AUTORISEES + "</p>");
                out.println("<p><a href='servlet-ListeRencontresEvol'>Retour</a></p>");
                out.println("</body></html>");
                return;
            }
        }

        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        out.println("<title>Liste des rencontres</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; cursor: pointer; }");
        out.println("th:hover { background-color: #45a049; }");
        out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
        out.println("tr:hover { background-color: #ddd; }");
        out.println("h2 { color: #333; margin-top: 30px; }");
        out.println(".info { background-color: #e7f3fe; border-left: 6px solid #2196F3; padding: 10px; margin: 20px 0; }");
        out.println(".equipe-info { background-color: #fff3cd; border-left: 6px solid #ffc107; padding: 15px; margin: 20px 0; }");
        out.println(".btn-changer { display: inline-block; margin-left: 10px; padding: 8px 15px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; }");
        out.println(".btn-changer:hover { background-color: #45a049; }");
        out.println(".btn-supprimer { padding: 6px 12px; background-color: #f44336; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }");
        out.println(".btn-supprimer:hover { background-color: #da190b; }");
        out.println("form.inline-form { margin: 0; display: inline; }");
        out.println("</style></head><body>");
        out.println("<h1>Liste des rencontres (avec tri)</h1>");

        if (colonneActuelle != null) {
            String sensTexte = sensActuel.equals("asc") ? "ascendant" : "descendant";
            out.println("<div class='info'>Tri actuel : <strong>" + escape(colonneActuelle) +
                       " (" + sensTexte + ")</strong></div>");
        } else {
            out.println("<div class='info'>Tri par défaut : <strong>jour (descendant), num_match</strong></div>");
        }

        try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            String nomEquipePreferee = null;
            if (equipePreferee != null) {
                String sqlNom = "SELECT nom_equipe FROM equipes WHERE num_equipe = ?";
                try (PreparedStatement pstmt = cnx.prepareStatement(sqlNom)) {
                    pstmt.setInt(1, Integer.parseInt(equipePreferee));
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            nomEquipePreferee = rs.getString("nom_equipe");
                        }
                    }
                }
            }

            if (nomEquipePreferee != null) {
                out.println("<div class='equipe-info'>");
                out.println("<strong>Filtre actif :</strong> Rencontres de <strong>" +
                           escape(nomEquipePreferee) + "</strong>");
                out.println("<a href='servlet-MonEquipe' class='btn-changer'>Changer d'équipe</a>");
                out.println("</div>");
            }

            String sql = "SELECT num_match, eq1, eq2, jour, sc1, sc2 " +
                         "FROM rencontres ";
            if (equipePreferee != null) {
                sql += "WHERE eq1 = ? OR eq2 = ? ";
            }
            sql += "ORDER BY " + orderBy;

            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                if (equipePreferee != null) {
                    pstmt = cnx.prepareStatement(sql);
                    int numEq = Integer.parseInt(equipePreferee);
                    pstmt.setInt(1, numEq);
                    pstmt.setInt(2, numEq);
                    rs = pstmt.executeQuery();
                } else {
                    pstmt = cnx.prepareStatement(sql);
                    rs = pstmt.executeQuery();
                }

                out.println("<table>");
                out.println("<thead><tr>");

                out.println(genererEnteteTri("num_match", "Numéro match", colonneActuelle, sensActuel));
                out.println(genererEnteteTri("eq1", "Équipe 1 (domicile)", colonneActuelle, sensActuel));
                out.println(genererEnteteTri("eq2", "Équipe 2 (extérieur)", colonneActuelle, sensActuel));
                out.println(genererEnteteTri("jour", "Date", colonneActuelle, sensActuel));
                out.println(genererEnteteTri("sc1", "Score Eq1", colonneActuelle, sensActuel));
                out.println(genererEnteteTri("sc2", "Score Eq2", colonneActuelle, sensActuel));
                out.println("<th>Action</th>");

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
                    out.println("<td>");
                    out.println("<form method='post' action='servlet-ListeRencontresEvol' class='inline-form'>");
                    out.println("<input type='hidden' name='num_match' value='" + numMatch + "'>");
                    out.println("<button type='submit' class='btn-supprimer'>Supprimer</button>");
                    out.println("</form>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</tbody></table>");
            } finally {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String numMatchStr = req.getParameter("num_match");

        if (numMatchStr != null && !numMatchStr.isEmpty()) {
            try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                String sql = "DELETE FROM rencontres WHERE num_match = ?";
                try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
                    pstmt.setInt(1, Integer.parseInt(numMatchStr));
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        res.sendRedirect("servlet-ListeRencontresEvol");
    }

    private String genererEnteteTri(String nomColonne, String libelleColonne,
                                   String colonneActuelle, String sensActuel) {
        String nouveauSens;
        String fleche;

        if (nomColonne.equals(colonneActuelle)) {
            nouveauSens = sensActuel.equals("asc") ? "desc" : "asc";
            fleche = sensActuel.equals("asc") ? " ↑" : " ↓";
        } else {
            nouveauSens = "asc";
            fleche = " ↕";
        }

        String url = "?tri=" + nomColonne + "&sens=" + nouveauSens;
        return "<th><a href='" + url + "' style='color: white; text-decoration: none;'>" +
               escape(libelleColonne) + fleche + "</a></th>";
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
