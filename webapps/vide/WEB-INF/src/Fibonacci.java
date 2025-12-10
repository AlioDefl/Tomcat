import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Fibonacci")
public class Fibonacci extends HttpServlet
{
  public void service( HttpServletRequest req, HttpServletResponse res )
       throws ServletException, IOException
  {
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();

    out.println("<!doctype html>");
    out.println("<head><title>Fibonacci</title></head><body><center>");
    out.println("<h1>Suite de Fibonacci (30 premiers)</h1>");

    int count = 30;
    int a = 1;
    int b = 1;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      if (i == 0 || i == 1) {
        sb.append(1);
      } else {
        int c = a + b;
        sb.append(c);
        a = b;
        b = c;
      }
      if (i < count - 1) sb.append(' ');
    }

    out.println("<p>" + sb + "</p>");
    out.println("</center> ");
    out.println("<ul> ");
    out.println("<li> Retour relatif : <a href=test.html>test.html</a> <p>");
    out.println("<li> Retour absolu :<a href=http://localhost:8080/vide/test.html>http://localhost:8080/vide/test.html</a> ");
    out.println("</ul> ");
    out.println("</body></html>");

  }
}