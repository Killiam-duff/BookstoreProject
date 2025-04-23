/**
 *
 * @author liamk
 */

import model.Book;
import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.util.*;
import java.util.stream.Collectors; // isnt being used but doesnt work without it

@WebServlet("/bookstore")
public class BookstoreServlet extends HttpServlet {
    private HashMap<Integer, Book> bookCache = new HashMap<>();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");

        if ("recommend".equals(action)) {
            handleRecommendations(request, response);
            return;
        }

        if ("aggregate".equals(action)) {
            handleAggregate(response);
            return;
        }

        if (action != null && !action.equals("recommend")) {
            handleAdminActions(request, out, action);
            return;
        }
        
        // Search logic
        String searchTitle = request.getParameter("title");
        String searchAuthor = request.getParameter("author");
        String searchCategory = request.getParameter("category");
        String searchBookId = request.getParameter("book_id");

        if ("all".equalsIgnoreCase(searchCategory)) searchCategory = null;

        List<Book> matchedBooks = new ArrayList<>();
        for (Book book : bookCache.values()) {
            boolean idMatch = (searchBookId == null || searchBookId.isEmpty()) || String.valueOf(book.getId()).equals(searchBookId);
            boolean titleMatch = (searchTitle == null || searchTitle.isEmpty()) || book.getTitle().toLowerCase().contains(searchTitle.toLowerCase());
            boolean authorMatch = (searchAuthor == null || searchAuthor.isEmpty()) || book.getAuthor().toLowerCase().contains(searchAuthor.toLowerCase());
            boolean categoryMatch = (searchCategory == null) || book.getCategory().equalsIgnoreCase(searchCategory);

            if (idMatch && titleMatch && authorMatch && categoryMatch) {
                matchedBooks.add(book);
            }
        }

        if (!matchedBooks.isEmpty()) {
            outputResults(out, matchedBooks, true);
            return;
        }

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;encrypt=false";
            Connection con = DriverManager.getConnection(url, "sa", "Magical.88");
            // search code, including join search \\
            String sql = "SELECT b.book_id, b.title, a.author_name AS author, c.category_name AS category " +
                         "FROM Book b JOIN Author a ON b.author_id = a.author_id " +
                         "JOIN Category c ON b.category_id = c.category_id WHERE 1=1";

            List<String> filters = new ArrayList<>();
            if (searchBookId != null && !searchBookId.trim().isEmpty()) {
                sql += " AND b.book_id = ?";
                filters.add("book_id");
            }
            if (searchTitle != null && !searchTitle.trim().isEmpty()) {
                sql += " AND b.title LIKE ?";
                filters.add("title");
            }
            if (searchAuthor != null && !searchAuthor.trim().isEmpty()) {
                sql += " AND a.author_name LIKE ?";
                filters.add("author");
            }
            if (searchCategory != null) {
                sql += " AND c.category_name = ?";
                filters.add("category");
            }

            sql += " ORDER BY b.title ASC";
            PreparedStatement ps = con.prepareStatement(sql);

            int index = 1;
            for (String filter : filters) {
                switch (filter) {
                    case "book_id": ps.setInt(index++, Integer.parseInt(searchBookId)); break;
                    case "title": ps.setString(index++, "%" + searchTitle + "%"); break;
                    case "author": ps.setString(index++, "%" + searchAuthor + "%"); break;
                    case "category": ps.setString(index++, searchCategory); break;
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String category = rs.getString("category");

                Book book = new Book(id, title, author, category);
                matchedBooks.add(book);
                bookCache.put(id, book);
            }

            rs.close();
            ps.close();
            con.close();

            outputResults(out, matchedBooks, false);

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
    // insert, update, delete code
    private void handleAdminActions(HttpServletRequest request, PrintWriter out, String action) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;encrypt=false";
            Connection con = DriverManager.getConnection(url, "sa", "Magical.88");
            PreparedStatement ps;

            switch (action) {
                case "insert":
                    int bookId = Integer.parseInt(request.getParameter("book_id"));
                    String title = request.getParameter("title");
                    int authorId = Integer.parseInt(request.getParameter("author_id"));
                    String authorName = request.getParameter("author_name");
                    int categoryId = Integer.parseInt(request.getParameter("category_id"));

                    ps = con.prepareStatement("IF NOT EXISTS (SELECT 1 FROM Author WHERE author_id = ?) " +
                                              "INSERT INTO Author (author_id, author_name) VALUES (?, ?)");
                    ps.setInt(1, authorId); ps.setInt(2, authorId); ps.setString(3, authorName);
                    ps.executeUpdate();

                    ps = con.prepareStatement("INSERT INTO Book (book_id, title, author_id, category_id) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, bookId); ps.setString(2, title); ps.setInt(3, authorId); ps.setInt(4, categoryId);
                    ps.executeUpdate();

                    out.println("<p style='color:lime;'>✅ Book inserted successfully.</p>");
                    break;

                case "update":
                    int updateBookId = Integer.parseInt(request.getParameter("book_id"));
                    String newTitle = request.getParameter("title");
                    int newAuthorId = Integer.parseInt(request.getParameter("author_id"));
                    String newAuthorName = request.getParameter("author_name");
                    int newCategoryId = Integer.parseInt(request.getParameter("category_id"));

                    ps = con.prepareStatement("IF NOT EXISTS (SELECT 1 FROM Author WHERE author_id = ?) " +
                                              "INSERT INTO Author (author_id, author_name) VALUES (?, ?)");
                    ps.setInt(1, newAuthorId); ps.setInt(2, newAuthorId); ps.setString(3, newAuthorName);
                    ps.executeUpdate();

                    ps = con.prepareStatement("UPDATE Book SET title = ?, author_id = ?, category_id = ? WHERE book_id = ?");
                    ps.setString(1, newTitle); ps.setInt(2, newAuthorId); ps.setInt(3, newCategoryId); ps.setInt(4, updateBookId);
                    ps.executeUpdate();

                    out.println("<p style='color:gold;'>✅ Book updated successfully.</p>");
                    break;

                case "delete":
                    ps = con.prepareStatement("DELETE FROM Book WHERE book_id = ?");
                    ps.setInt(1, Integer.parseInt(request.getParameter("book_id")));
                    ps.executeUpdate();

                    out.println("<p style='color:red;'>❌ Book deleted successfully.</p>");
                    break;
            }

            bookCache.clear();
            con.close();

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
    // advanced function
    private void handleRecommendations(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String category = request.getParameter("category");

        if (category == null || category.trim().isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("[]");
            return;
        }

        List<Book> recommendations = new ArrayList<>();

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;encrypt=false";
            Connection con = DriverManager.getConnection(url, "sa", "Magical.88");
            // advanced function code
            String sql = "SELECT TOP 3 b.book_id, b.title, a.author_name, c.category_name " +
                         "FROM Book b JOIN Author a ON b.author_id = a.author_id " +
                         "JOIN Category c ON b.category_id = c.category_id " +
                         "WHERE c.category_name = ? ORDER BY NEWID()";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                recommendations.add(new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author_name"),
                    rs.getString("category_name")
                ));
            }

            rs.close(); ps.close(); con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("[");
        for (int i = 0; i < recommendations.size(); i++) {
            Book b = recommendations.get(i);
            out.print(String.format("{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\"}",
                    b.getId(), escape(b.getTitle()), escape(b.getAuthor()), escape(b.getCategory())));
            if (i < recommendations.size() - 1) out.print(",");
        }
        out.print("]");
    }
    // aggregate function
    private void handleAggregate(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;encrypt=false";
            Connection con = DriverManager.getConnection(url, "sa", "Magical.88");
            // aggregate function code, using count
            String sql = "SELECT c.category_name, COUNT(*) AS total FROM Book b " +
                         "JOIN Category c ON b.category_id = c.category_id GROUP BY c.category_name";

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            out.println("<html><head><title>Category Totals</title></head><body>");
            out.println("<h2>Book Count per Category</h2>");
            out.println("<table border='1'><tr><th>Category</th><th>Total Books</th></tr>");

            while (rs.next()) {
                out.printf("<tr><td>%s</td><td>%d</td></tr>%n",
                        rs.getString("category_name"), rs.getInt("total"));
            }

            out.println("</table>");
            out.println("<br><button onclick=\"window.location.href='index.html'\">⬅ Back to Home</button>");
            out.println("</body></html>");

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }

    private void outputResults(PrintWriter out, List<Book> books, boolean fromCache) {
        out.println("<html><head><title>Search Results</title></head><body>");
        out.println("<h2>Search Results</h2>");
        if (fromCache) out.println("<p style='color: green;'>[From Cache]</p>");

        if (books.isEmpty()) {
            out.println("<p>No matching books found.</p>");
        } else {
            out.println("<table border='1' style='font-size: 14px;'><tr><th>ID</th><th>Title</th><th>Author</th><th>Category</th></tr>");
            for (Book book : books) {
                out.printf("<tr><td>%d</td><td><a href='buy.html?bookId=%d'>%s</a></td><td>%s</td><td>%s</td></tr>%n",
                        book.getId(), book.getId(), book.getTitle(), book.getAuthor(), book.getCategory());
            }
            out.println("</table>");
        }

        out.println("<br><button onclick=\"window.location.href='index.html'\">⬅ Back to Home</button>");
        out.println("</body></html>");
    }
}


