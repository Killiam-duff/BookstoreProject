/**
 *
 * @author liamk
 */
import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/bookstore")
public class BookstoreServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String searchType = request.getParameter("searchType"); // bookid, title, author, category
        String category = request.getParameter("category"); // all, fiction, nonfiction, etc.
        String query = request.getParameter("query"); // user input

        try {
            String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;integratedSecurity=true;encrypt=false";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection con = DriverManager.getConnection(url);

            // Build the base query
            String sql = "SELECT book_id, title, author, category FROM Book WHERE 1=1";
            if (query != null && !query.isEmpty()) {
                sql += " AND " + searchType + " LIKE ?";
            }
            if (category != null && !category.equals("all")) {
                sql += " AND category = ?";
            }

            PreparedStatement stmt = con.prepareStatement(sql);

            int paramIndex = 1;
            if (query != null && !query.isEmpty()) {
                stmt.setString(paramIndex++, "%" + query + "%");
            }
            if (category != null && !category.equals("all")) {
                stmt.setString(paramIndex++, category);
            }

            ResultSet rs = stmt.executeQuery();

            out.println("<html><head><title>Search Results</title></head><body>");
            out.println("<h1>Search Results</h1>");
            out.println("<table border='1'><tr><th>ID</th><th>Title</th><th>Author</th><th>Category</th></tr>");

            boolean found = false;
            while (rs.next()) {
                found = true;
                out.println("<tr><td>" + rs.getInt("book_id") + "</td>");
                out.println("<td>" + rs.getString("title") + "</td>");
                out.println("<td>" + rs.getString("Author") + "</td>");
                out.println("<td>" + rs.getString("category") + "</td></tr>");
            }

            if (!found) {
                out.println("<tr><td colspan='4'>No results found</td></tr>");
            }

            out.println("</table>");
            out.println("</body></html>");

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}
