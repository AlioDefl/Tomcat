import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/servlet-Ascii")
public class Ascii extends HttpServlet
{
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    processRequest(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    processRequest(req, res);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/html;charset=UTF-8");
    PrintWriter out = res.getWriter();

    String nbColParam = req.getParameter("nbCol");
    int nbCol = 1; 

    if (nbColParam != null && !nbColParam.isEmpty()) {
      try {
        nbCol = Integer.parseInt(nbColParam);
        if (nbCol < 1 || nbCol > 14) {
          nbCol = 1;
        }
      } catch (NumberFormatException e) {
        nbCol = 1;
      }
    }

    out.println("<!doctype html>");
    out.println("<head><title>Table ASCII</title><meta charset=\"UTF-8\">");
    out.println("<style>");
    out.println("table{border-collapse:collapse} td,th{border:1px solid #ccc;padding:4px 8px;font:14px sans-serif;text-align:left}");
    out.println("th{background:#f3f3f3}");
    out.println("a{margin:5px;padding:5px 10px;text-decoration:none;border:1px solid #ccc;display:inline-block;background:#f0f0f0}");
    out.println("a:hover{background:#e0e0e0}");
    out.println("ul>li{list-style-type: none}");
    out.println("form{margin:20px 0}");
    out.println("select{padding:5px;font-size:14px}");
    out.println("input[type=submit]{padding:5px 15px;font-size:14px;margin-left:10px}");
    out.println("</style></head>");
    out.println("<body><center>");
    out.println("<h1>Codes ASCII (32 à 255) - " + nbCol + " colonne(s)</h1>");

    // Formulaire avec SELECT utilisant POST
    out.println("<form method='POST' action='servlet-Ascii'>");
    out.println("<label for=\"nbCol\">Nombre de colonnes : </label>");
    out.println("<select name=\"nbCol\" id=\"nbCol\">");
    for (int i = 1; i <= 14; i++) {
      String selected = (i == nbCol) ? " selected" : "";
      out.println("<option value=\"" + i + "\"" + selected + ">" + i + " colonne(s)</option>");
    }
    out.println("</select>");
    out.println("<input type=\"submit\" value=\"Valider\">");
    out.println("</form>");

    out.println("<div>");
    for (int i = 1; i <= 14; i++) {
      String linkStyle = (i == nbCol) ? " style=\"background:#ccc;font-weight:bold;\"" : "";
      out.println("<a href=\"?nbCol=" + i + "\"" + linkStyle + ">" + i + " colonne(s)</a>");
    }
    out.println("</div><br>");

    out.println("<table>");
    out.println("<tr>");
    for (int col = 0; col < nbCol; col++) {
      out.println("<th>Code</th><th>Caractère</th>");
    }
    out.println("</tr>");

    int totalCodes = 255 - 32 + 1;
    int nbLignes = (int) totalCodes / nbCol;

    for (int ligne = 0; ligne < nbLignes; ligne++) {
      out.println("<tr>");
      for (int col = 0; col < nbCol; col++) {
        int code = 32 + ligne * nbCol + col;
        if (code <= 255) {
          char ch = (char) code;
          out.println("<td>" + code + "</td><td>" + String.valueOf(ch) + "</td>");
        } else {
          out.println("<td></td><td></td>");
        }
      }
      out.println("</tr>");
    }
    out.println("</table>");

    out.println("<br><ul>");
    out.println("<li> Retour relatif : <a href=test.html>test.html</a>");
    out.println("<li> Retour absolu : <a href=http://localhost:8080/vide/test.html>http://localhost:8080/vide/test.html</a>");
    out.println("</ul>");

    out.println("</center></body></html>");
  }
}
