package model;

/**
 *
 * @author liamk
 */

public class Book {
    private int id;
    private String title;
    private String author;
    private String category;

    public Book(int id, String title, String author, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
}

