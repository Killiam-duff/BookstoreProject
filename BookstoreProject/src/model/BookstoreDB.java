
/**
 *
 * @author liamk
 */
package model;
import java.sql.*;

public class BookstoreDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlserver://localhost:1434;instanceName=SQLEXPRESS;databaseName=Bookstore;encrypt=false";
        String user = "sa";
        String password = "Magical.88";

        Connection con = DriverManager.getConnection(url, user, password);
        Statement st = con.createStatement();

        String[] dropStatements = {
            "DROP TABLE IF EXISTS student_book_tr;",
            "DROP TABLE IF EXISTS Inventory;",
            "DROP TABLE IF EXISTS Book;",
            "DROP TABLE IF EXISTS Category;",
            "DROP TABLE IF EXISTS Author;",
            "DROP TABLE IF EXISTS Student;"
        };
        for (String sql : dropStatements) {
            st.executeUpdate(sql);
        }

        st.executeUpdate("CREATE TABLE Category (category_id INT PRIMARY KEY, category_name VARCHAR(100))");
        st.executeUpdate("CREATE TABLE Author (author_id INT PRIMARY KEY, author_name VARCHAR(100))");
        st.executeUpdate("CREATE TABLE Student (student_id INT PRIMARY KEY, student_name VARCHAR(100))");

        st.executeUpdate("CREATE TABLE Book (" +
                "book_id INT PRIMARY KEY, " +
                "title VARCHAR(100), " +
                "author_id INT, " +
                "category_id INT, " +
                "FOREIGN KEY(author_id) REFERENCES Author(author_id), " +
                "FOREIGN KEY(category_id) REFERENCES Category(category_id))");

        st.executeUpdate("CREATE TABLE Inventory (book_id INT PRIMARY KEY, Quantity INTEGER, FOREIGN KEY(book_id) REFERENCES Book(book_id))");
        st.executeUpdate("CREATE TABLE student_book_tr (" +
                "transaction_id INT PRIMARY KEY, " +
                "student_id INT, " +
                "book_id INT, " +
                "FOREIGN KEY(student_id) REFERENCES Student(student_id), " +
                "FOREIGN KEY(book_id) REFERENCES Book(book_id))");

        // Insert categories
        st.executeUpdate("INSERT INTO Category VALUES " +
            "(1, 'Fiction'), " +
            "(2, 'Non-fiction'), " +
            "(3, 'Educational')");

        // Insert authors
        st.executeUpdate("INSERT INTO Author VALUES " +
            "(48, 'John Dewey'), (33, 'Paul Lockhart'), (82, 'John V. Guttag'), " +
            "(60, 'William Stallings'), (97, 'J. R. R. Tolkien'), (37, 'Joseph Migga Kizza'), " +
            "(83, 'George Orwell'), (64, 'Stephen Hawking'), (47, 'Michael Lewis'), (69, 'Frank Herbert'), " +
            "(23, 'Carlo Rovelli'), (12, 'Lawrence M. Krauss'), (7, 'David S. Goodsell'), (6, 'Richard Dawkins'), " +
            "(22, 'Sian E. Harding'), (10, 'Bud Watson'), (17, 'Ismael Perez'), (25, 'Bjarne Stroustrup'), " +
            "(78, 'Thomas Paine'), (21, 'John Holl'), (67, 'Kyle Loudon'), (34, 'Dmitry Danilov'), " +
            "(36, 'Betty Schrampfer Azar'), (29, 'Daniel A. Sjursen'), (71, 'Morris Kline'), (11, 'Dave Eggers')");

        // Insert students
        st.executeUpdate("INSERT INTO Student VALUES (1, 'Alice Johnson'), (2, 'Bob Williams')");

        // Insert books
        st.executeUpdate("INSERT INTO Book (book_id, title, author_id, category_id) VALUES " +
            "(1, 'Dune', 69, 1), " +
            "(2, 'Dune Messiah', 69, 1), " +
            "(3, 'Children of Dune', 69, 1), " +
            "(4, 'Seven Brief Lessons on Physics', 23, 3), " +
            "(5, 'A Universe from Nothing', 12, 3), " +
            "(6, 'The Machinery of Life', 7, 3), " +
            "(7, 'The Selfish Gene', 6, 3), " +
            "(8, 'The Exquisite Machine', 22, 3), " +
            "(9, 'The Sigma Male Bible', 10, 2), " +
            "(10, 'The Secret Government', 17, 2), " +
            "(11, 'A Tour of C++', 25, 3), " +
            "(12, 'Common Sense', 78, 2), " +
            "(13, 'Drink Beer, Think Beer', 21, 2), " +
            "(14, 'C++ Pocket Reference', 67, 3), " +
            "(15, 'Refactoring with C++', 34, 3), " +
            "(16, 'Computer Security: Principles and Practice', 60, 3), " +
            "(17, 'Cryptography and Network Security: Principles and Practice', 60, 3), " +
            "(18, 'The Little Book of Semaphores', 82, 3), " +
            "(19, 'Introduction to Computation and Programming Using Python', 82, 3), " +
            "(20, 'Measurement', 33, 3), " +
            "(21, 'Arithmetic', 33, 3), " +
            "(22, 'The Child and the Curriculum', 48, 3), " +
            "(23, 'Experience and Education', 48, 3), " +
            "(24, 'Democracy and Education', 48, 3), " +
            "(25, 'The Fellowship of the Ring', 97, 1), " +
            "(26, 'The Two Towers', 97, 1), " +
            "(27, 'The Return of the King', 97, 1), " +
            "(28, 'Guide to Computer Network Security', 37, 3), " +
            "(29, 'Ethics in Computing', 37, 3)");

        // Insert inventory and transactions
        st.executeUpdate("INSERT INTO Inventory VALUES (1, 50), (2, 30), (3, 20)");
        st.executeUpdate("INSERT INTO student_book_tr VALUES (101, 1, 1), (102, 2, 2)");

        System.out.println("✅ Bookstore database created successfully.");

        st.close();
        con.close();
    }
}



