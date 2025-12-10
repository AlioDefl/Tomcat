// Servlet Test.java  de test de la configuration
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Test")
public class Test extends HttpServlet
{
  public void service( HttpServletRequest req, HttpServletResponse res ) 
       throws ServletException, IOException
  {
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();
    out.println("<!doctype html>");
    out.println("<head><title>servlet Test</title></head><body><center> ");
    out.println("<h1>Test de la Servlet Java</h1> ");
    out.println("<br>");
    out.println("<blink>Une erreur s'est glissée dans cette page. Saurez vous la corriger ?</blink>");
    out.println("<br>");
    out.println("<br>");
    out.println("<br>Testez le retour vers la page html<br> ");
    out.println("</center> ");
    out.println("<ul> ");
    out.println("<li> Retour relatif : <a href=test.html>test.html</a> <p>");
    out.println("<li> Retour absolu :<a href=http://localhost:8080/vide/test.html>http://localhost:8080/vide/test.html</a> ");
    out.println("</ul> ");
    out.println("</body></html> ");
  }
}
