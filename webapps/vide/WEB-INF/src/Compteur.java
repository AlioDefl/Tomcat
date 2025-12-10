import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Compteur")
public class Compteur extends HttpServlet {
    
    private int compteurGlobal = 0;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);
        
        Integer compteurLocal = (Integer) session.getAttribute("compteurLocal");
        if (compteurLocal == null) {
            compteurLocal = 0;
        }
        compteurLocal++;
        session.setAttribute("compteurLocal", compteurLocal);
        
        synchronized (this) {
            compteurGlobal++;
        }
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Compteur de visites</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 20px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Compteur</h1>");
        out.println("<p>Vous avez accédé " + compteurLocal + " fois à cette page sur les "
                    + compteurGlobal + " accès au total.</p>");
        out.println("</body>");
        out.println("</html>");
    }
}