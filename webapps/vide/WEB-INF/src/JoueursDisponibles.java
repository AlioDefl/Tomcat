import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-JoueursDisponibles")
public class JoueursDisponibles extends HttpServlet {
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

        // Vérifier le cookie d'équipe favorite
        Integer numEquipeFavorite = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("equipeFavorite".equals(cookie.getName())) {
                    try {
                        numEquipeFavorite = Integer.parseInt(cookie.getValue());
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
            }
        }

        // Si pas de cookie, rediriger vers ChoisirEquipe
        if (numEquipeFavorite == null) {
            response.sendRedirect("servlet-ChoisirEquipe");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String poste = request.getParameter("poste");
        if (poste == null || poste.isEmpty()) {
            poste = "ATT";
        }

        HttpSession session = request.getSession(true);
        @SuppressWarnings("unchecked")
        Set<Integer> joueursChoisis = (Set<Integer>) session.getAttribute("joueursChoisis");
        if (joueursChoisis == null) {
            joueursChoisis = new HashSet<>();
            session.setAttribute("joueursChoisis", joueursChoisis);
        }

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Joueurs Disponibles</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; max-width: 600px; margin: 20px auto; }");
        out.println("h1 { text-align: center; }");
        out.println("p { text-align: center; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Joueurs - " + poste + "</h1>");

        out.println("<p>");
        String[] postes = {"GAR", "DEF", "MIL", "ATT"};
        for (String p : postes) {
            out.println("<a href='servlet-JoueursDisponibles?poste=" + p + "'>" + p + "</a> ");
        }
        out.println("</p>");

        out.println("<p><a href='servlet-JoueursChoisis'>Ma Dream Team (" + joueursChoisis.size() + ")</a></p>");

        try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {

            // Récupérer le nom de l'équipe pour l'affichage
            String nomEquipe = "";
            String sqlNomEquipe = "SELECT nom_equipe FROM equipes WHERE num_equipe = ?";
            try (PreparedStatement pstmt = cnx.prepareStatement(sqlNomEquipe)) {
                pstmt.setInt(1, numEquipeFavorite);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        nomEquipe = rs.getString("nom_equipe");
                    }
                }
            }

            out.println("<p>Équipe favorite : " + nomEquipe + " - <a href='servlet-ChoisirEquipe'>Changer</a></p>");

            String sql;
            if (joueursChoisis.isEmpty()) {
                sql = "SELECT num_joueur, maillot, nom_joueur FROM joueurs WHERE poste = ? AND club = ? ORDER BY nom_joueur";
            } else {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < joueursChoisis.size(); i++) {
                    if (i > 0) placeholders.append(",");
                    placeholders.append("?");
                }
                sql = "SELECT num_joueur, maillot, nom_joueur FROM joueurs WHERE poste = ? AND club = ? AND num_joueur NOT IN (" + placeholders + ") ORDER BY nom_joueur";
            }

            try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
                pstmt.setString(1, poste);
                pstmt.setInt(2, numEquipeFavorite);

                int index = 3;
                for (Integer numJoueur : joueursChoisis) {
                    pstmt.setInt(index++, numJoueur);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    out.println("<ul>");
                    while (rs.next()) {
                        int numJoueur = rs.getInt("num_joueur");
                        int maillot = rs.getInt("maillot");
                        String nomJoueur = rs.getString("nom_joueur");

                        out.println("<li>#" + maillot + " " + nomJoueur +
                                  " - <a href='servlet-JoueursChoisis?joueur=" + numJoueur + "'>Sélectionner</a></li>");
                    }
                    out.println("</ul>");
                }
            }

        } catch (SQLException ex) {
            out.println("<p>Erreur : " + ex.getMessage() + "</p>");
        }

        out.println("</body>");
        out.println("</html>");
    }
}
