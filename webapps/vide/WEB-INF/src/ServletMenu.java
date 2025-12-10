import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Menu")
public class ServletMenu extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userLogin") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String login = (String) session.getAttribute("userLogin");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Menu</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 40px; }");
        out.println("a { display: block; margin: 10px 0; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<h1>Menu principal</h1>");
        out.println("<p>Bienvenue <strong>" + login + "</strong></p>");
        out.println("<p><a href='servlet-Lecture'>Consulter mes coordonnées</a></p>");
        out.println("<p><a href='servlet-Modif'>Modifier mes coordonnées</a></p>");
        out.println("<br>");
        out.println("<p><a href='servlet-Deconnecte'>Déconnexion</a></p>");
        out.println("</body></html>");
    }
}
