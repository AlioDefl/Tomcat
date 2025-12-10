import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-JoueursChoisis")
public class JoueursChoisis extends HttpServlet {
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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);

        @SuppressWarnings("unchecked")
        Set<Integer> joueursChoisis = (Set<Integer>) session.getAttribute("joueursChoisis");
        if (joueursChoisis == null) {
            joueursChoisis = new HashSet<>();
            session.setAttribute("joueursChoisis", joueursChoisis);
        }

        String reset = request.getParameter("reset");
        if ("true".equals(reset)) {
            joueursChoisis.clear();
            session.setAttribute("joueursChoisis", joueursChoisis);
        }

        String joueurParam = request.getParameter("joueur");
        if (joueurParam != null && !joueurParam.isEmpty()) {
            try {
                int numJoueur = Integer.parseInt(joueurParam);
                if (!joueursChoisis.contains(numJoueur)) {
                    joueursChoisis.add(numJoueur);
                    session.setAttribute("joueursChoisis", joueursChoisis);
                }
            } catch (NumberFormatException e) {
            }
        }

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Ma Dream Team</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; max-width: 600px; margin: 20px auto; }");
        out.println("h1 { text-align: center; }");
        out.println("p { text-align: center; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Ma Dream Team</h1>");

        out.println("<p>");
        out.println("<a href='servlet-JoueursDisponibles'>Retour aux joueurs</a> | ");
        out.println("<a href='servlet-JoueursChoisis?reset=true'>Réinitialiser</a>");
        out.println("</p>");

        out.println("<p>" + joueursChoisis.size() + " joueur(s)</p>");

        if (joueursChoisis.isEmpty()) {
            out.println("<p>Votre équipe est vide.</p>");
        } else {
            try (Connection cnx = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {

                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < joueursChoisis.size(); i++) {
                    if (i > 0) placeholders.append(",");
                    placeholders.append("?");
                }

                String sql = "SELECT num_joueur, nom_joueur, poste, maillot FROM joueurs WHERE num_joueur IN (" + placeholders + ") ORDER BY poste, nom_joueur";

                try (PreparedStatement pstmt = cnx.prepareStatement(sql)) {
                    int index = 1;
                    for (Integer numJoueur : joueursChoisis) {
                        pstmt.setInt(index++, numJoueur);
                    }

                    try (ResultSet rs = pstmt.executeQuery()) {
                        out.println("<ul>");
                        while (rs.next()) {
                            String nomJoueur = rs.getString("nom_joueur");
                            String poste = rs.getString("poste");
                            int maillot = rs.getInt("maillot");

                            out.println("<li>" + poste + " - #" + maillot + " " + nomJoueur + "</li>");
                        }
                        out.println("</ul>");
                    }
                }

            } catch (SQLException ex) {
                out.println("<p>Erreur : " + ex.getMessage() + "</p>");
            }
        }

        out.println("</body>");
        out.println("</html>");
    }
}
