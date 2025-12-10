import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.time.*;
import java.time.temporal.ChronoUnit;

@WebServlet("/servlet-NouvelAn")
public class NouvelAn extends HttpServlet {
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();

    LocalDateTime now = LocalDateTime.now();
    int nextYear = now.getYear() + 1;
    LocalDateTime jan1NextYear = LocalDateTime.of(nextYear, Month.JANUARY, 1, 0, 0);
    long seconds = Duration.between(now, jan1NextYear).get(ChronoUnit.SECONDS);
    if (seconds < 0) seconds = 0;

    out.println("<!doctype html>");
    out.println("<head><title>Nouvel An</title>");
    out.println("<meta http-equiv=refresh content=2>");
    out.println("<style>body{font:16px/1.4 sans-serif} .box{margin:40px auto;max-width:540px;text-align:center} .num{font-size:40px;font-weight:700}</style></head>");
    out.println("<body><div class=\"box\"><h1>Secondes avant le 1er Janvier " + nextYear + "</h1>");
    out.println("<div class=\"num\">" + seconds + "</div>");
    out.println("<p>Actualisation automatique toutes les 2 secondes</p>");
    out.println("<p><a href=test.html>Retour test.html</a></p>");
    out.println("</div></body></html>");
  }
}
