import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Statut")
public class ServletStatut extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Statut</title>");
        out.println("<style>body { font-family: Arial; margin: 40px; }</style>");
        out.println("</head><body>");

        if (session != null && session.getAttribute("userLogin") != null) {
            out.println("<h1>Utilisateur connu du SGBD</h1>");
            out.println("<p>Bienvenue <strong>" + session.getAttribute("userLogin") + "</strong></p>");
        } else {
            out.println("<h1>Inconnu</h1>");
        }

        out.println("<br><a href='login.html'>Retour au login</a>");
        out.println("</body></html>");
    }
}
