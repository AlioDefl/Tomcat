import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
@WebServlet("/servlet-First")
public class First extends HttpServlet
    {
    public void service( HttpServletRequest req, HttpServletResponse res )
    throws ServletException, IOException
        {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.println( "<head><title>servlet first</title>" );
        out.println( "<META content=\"charset=UTF-8\"></head><body><center>" );
        out.println( "<h1>Test de ma Servlet</h1>" );
        out.println( "<h2>Super ! ça marche</h2>" );
        out.println( "</center> </body>" );
    }
}