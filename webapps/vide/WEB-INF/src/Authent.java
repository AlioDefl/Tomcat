import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Authent")
public class Authent extends HttpServlet {
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
            throw new ServletException(" introuvable : " + jdbcDriver, e);
        } catch (IOException e) {
            throw new ServletException("échec: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String login = request.getParameter("login");
        String mdp = request.getParameter("mdp");

        HttpSession session = request.getSession();
        boolean authentifie = false;
        String role = null;

        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            // Comparaison avec le hash MD5 du mot de passe fourni
            String sql = "SELECT role FROM personne WHERE login = ? AND mdp = MD5(?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, login);
                pstmt.setString(2, mdp);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        authentifie = true;
                        role = rs.getString("role");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (authentifie) {
            session.setAttribute("userLogin", login);
            session.setAttribute("userRole", role);
            session.setMaxInactiveInterval(10);
            response.sendRedirect("servlet-Menu");
        } else {
            session.removeAttribute("userLogin");
            session.removeAttribute("userRole");
            response.sendRedirect("login.html");
        }
    }
}
