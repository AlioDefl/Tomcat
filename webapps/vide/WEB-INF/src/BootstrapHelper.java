public class BootstrapHelper {
    public static String getHeader(String titre) {
        return "<!doctype html>\n" +
               "<html lang='fr'>\n" +
               "<head>\n" +
               "    <meta charset='utf-8'>\n" +
               "    <meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
               "    <title>" + titre + "</title>\n" +
               "    <link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css' rel='stylesheet'>\n" +
               "</head>\n" +
               "<body>\n" +
               "<div class='container mt-4'>\n";
    }
    
    public static String getFooter() {
        return "</div>\n</body>\n</html>";
    }
}
