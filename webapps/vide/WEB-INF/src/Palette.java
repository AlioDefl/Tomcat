import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Palette")
public class Palette extends HttpServlet
{
  private static final String[] HEX = {
    "0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"
  };

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();

    String rParam = req.getParameter("r");
    int r = 0;
    
    if (rParam != null && !rParam.isEmpty()) {
      try {
        r = Integer.parseInt(rParam);
        if (r < 0 || r > 15) {
          r = 0;
        }
      } catch (NumberFormatException e) {
        r = 0;
      }
    }

    out.println("<!doctype html>");
    out.println("<head><title>Palette</title><style>");
    out.println("table{border-collapse:collapse} td{width:28px;height:28px;border:1px solid #ccc;text-align:center;font:12px sans-serif}");
    out.println("a{margin:5px;padding:5px 10px;text-decoration:none;border:1px solid #ccc;display:inline-block;background:#f0f0f0}");
    out.println("a:hover{background:#e0e0e0}");
    out.println("</style></head><body><center>");
    out.println("<h1>Palette (Rouge = " + r + ")</h1>");

    out.println("<div>");
    for (int i = 0; i < 16; i++) {
      String linkClass = (i == r) ? " style=\"background:#ccc;font-weight:bold;\"" : "";
      out.println("<a href=\"?r=" + i + "\"" + linkClass + ">R=" + i + "</a>");
    }
    out.println("</div><br>");

    out.println("<table>");
    for (int v = 0; v < 16; v++) {
      out.println("<tr>");
      for (int b = 0; b < 16; b++) {
        String color = "#" + HEX[r] + HEX[v] + HEX[b]; 
        out.println("<td style=\"background:" + color + ";\" title=\"" + color + "\">" +  "</td>");
      }
      out.println("</tr>");
    }
    out.println("</table>");

    out.println("<br><ul>");
    out.println("<li> Retoursdsdr relatif : <a href=test.html>test.html</a>");
    out.println("<li> Retour absolu : <a href=http://localhost:8080/vide/test.html>http://localhost:8080/vide/test.html</a>");
    out.println("</ul>");

    out.println("</center></body></html>");
  }
}
