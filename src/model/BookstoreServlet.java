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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/bookstore")
public class BookstoreServlet extends HttpServlet {
    private HashMap<Integer, Book> bookCache = new HashMap<>();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");

        if (action != null) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;encrypt=false";
                String user = "sa";
                String password = "Magical.88";
                Connection con = DriverManager.getConnection(url, user, password);
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
                        ps.setInt(1, authorId);
                        ps.setInt(2, authorId);
                        ps.setString(3, authorName);
                        ps.executeUpdate();

                        ps = con.prepareStatement("INSERT INTO Book (book_id, title, author_id, category_id) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, bookId);
                        ps.setString(2, title);
                        ps.setInt(3, authorId);
                        ps.setInt(4, categoryId);
                        ps.executeUpdate();

                        out.println("<p style='color:lime;'>✅ Book inserted successfully.</p>");
                        break;
                    case "update":
                        ps = con.prepareStatement("UPDATE Book SET title = ? WHERE book_id = ?");
                        ps.setString(1, request.getParameter("title"));
                        ps.setInt(2, Integer.parseInt(request.getParameter("book_id")));
                        ps.executeUpdate();
                        out.println("<p style='color:gold;'>Book title updated successfully.</p>");
                        break;
                    case "delete":
                        ps = con.prepareStatement("DELETE FROM Book WHERE book_id = ?");
                        ps.setInt(1, Integer.parseInt(request.getParameter("book_id")));
                        ps.executeUpdate();
                        out.println("<p style='color:red;'>Book deleted successfully.</p>");
                        break;
                    default:
                        out.println("<p>Unknown action.</p>");
                }

                bookCache.clear();
                con.close();
            } catch (Exception e) {
                e.printStackTrace(out);
            }
            return;
        }

        // Regular Search Handling
        String searchTitle = request.getParameter("title");
        String searchAuthor = request.getParameter("author");
        String searchCategory = request.getParameter("category");
        String searchBookId = request.getParameter("book_id");

        if (searchCategory != null && (searchCategory.equalsIgnoreCase("all") || searchCategory.trim().isEmpty())) {
            searchCategory = null;
        }

        List<Book> matchedBooks = new ArrayList<>();

        for (Book book : bookCache.values()) {
            boolean idMatch = (searchBookId == null || searchBookId.trim().isEmpty()) ||
                    String.valueOf(book.getId()).equals(searchBookId);
            boolean titleMatch = (searchTitle == null || searchTitle.trim().isEmpty()) ||
                    book.getTitle().toLowerCase().contains(searchTitle.toLowerCase());
            boolean authorMatch = (searchAuthor == null || searchAuthor.trim().isEmpty()) ||
                    book.getAuthor().toLowerCase().contains(searchAuthor.toLowerCase());
            boolean categoryMatch = (searchCategory == null) ||
                    book.getCategory().equalsIgnoreCase(searchCategory);

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
            String user = "sa";
            String password = "Magical.88";

            Connection con = DriverManager.getConnection(url, user, password);

            String sql = "SELECT b.book_id, b.title, a.author_name AS author, c.category_name AS category " +
                         "FROM Book b " +
                         "JOIN Author a ON b.author_id = a.author_id " +
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
                    case "book_id":
                        ps.setInt(index++, Integer.parseInt(searchBookId));
                        break;
                    case "title":
                        ps.setString(index++, "%" + searchTitle + "%");
                        break;
                    case "author":
                        ps.setString(index++, "%" + searchAuthor + "%");
                        break;
                    case "category":
                        ps.setString(index++, searchCategory);
                        break;
                }
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String category = rs.getString("category");

                Book book = new Book(bookId, title, author, category);
                matchedBooks.add(book);
                bookCache.put(bookId, book);
            }

            rs.close();
            ps.close();
            con.close();

            outputResults(out, matchedBooks, false);

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    private void outputResults(PrintWriter out, List<Book> books, boolean fromCache) {
        out.println("<html><head><title>Search Results</title></head><body>");
        out.println("<h2>Search Results</h2>");
        if (fromCache) {
            out.println("<p style='color: green;'>[From Cache]</p>");
        }

        if (books.isEmpty()) {
            out.println("<p>No matching books found.</p>");
        } else {
            out.println("<table border='1' style='font-size: 14px;'><tr><th>ID</th><th>Title</th><th>Author</th><th>Category</th></tr>");
            for (Book book : books) {
                out.println("<tr>");
                out.println("<td>" + book.getId() + "</td>");
                out.println("<td><a href='buy.html?bookId=" + book.getId() + "'>" + book.getTitle() + "</a></td>");
                out.println("<td>" + book.getAuthor() + "</td>");
                out.println("<td>" + book.getCategory() + "</td>");
                out.println("</tr>");
            }
            out.println("</table>");
            out.println("<br><button onclick=\"window.location.href='index.html'\">⬅ Back to Home</button>");
            out.println("<br><br><h3>📍 Find a bookstore near you:</h3>");
            out.println("<iframe width='600' height='450' style='border:0' loading='lazy' allowfullscreen "
                    + "src='https://www.google.com/maps/embed/v1/search?key=AIzaSyCQiqzC6k7O9RJwrSiLVb-x2940mzhJxTw&q=bookstore+in+Tallahassee'></iframe>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}



