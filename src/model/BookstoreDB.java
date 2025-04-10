
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

        st.executeUpdate("INSERT INTO Category VALUES (1, 'Fiction'), (2, 'Non-fiction'), (3, 'Educational')");
        st.executeUpdate("INSERT INTO Author VALUES (1, 'Isaac Asimov'), (2, 'J.K. Rowling'), (3, 'Yuval Noah Harari')");
        st.executeUpdate("INSERT INTO Student VALUES (1, 'Alice Johnson'), (2, 'Bob Williams')");

        st.executeUpdate("INSERT INTO Book (book_id, title, author_id, category_id) VALUES " +
                "(1, 'Introduction to Algorithms', 1, 3), " +
                "(2, 'Harry Potter and the Sorcerer''s Stone', 2, 1), " +
                "(3, 'Sapiens: A Brief History of Humankind', 3, 2)");

        st.executeUpdate("INSERT INTO Inventory VALUES (1, 50), (2, 30), (3, 20)");
        st.executeUpdate("INSERT INTO student_book_tr VALUES (101, 1, 1), (102, 2, 2)");

        System.out.println("Bookstore database created Successfully. ");

        st.close();
        con.close();
    }
}



