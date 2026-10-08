package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/books")
public class BrowseBooksServlet extends HttpServlet {

    public static class Book {
        private int id;
        private String title;
        private String author;
        private String isbn;
        private String genre;
        private int totalCopies;
        private int availableCopies;

        public Book(int id, String title, String author, String isbn,
                    String genre, int totalCopies, int availableCopies) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.genre = genre;
            this.totalCopies = totalCopies;
            this.availableCopies = availableCopies;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getIsbn() {
            return isbn;
        }

        public String getGenre() {
            return genre;
        }

        public int getTotalCopies() {
            return totalCopies;
        }

        public int getAvailableCopies() {
            return availableCopies;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");

        List<Book> books = new ArrayList<>();

        String sql;

        if (search == null || search.trim().isEmpty()) {

            sql = """
                    SELECT id, title, author, isbn, genre,
                           total_copies, available_copies
                    FROM books
                    ORDER BY title
                    """;

        } else {

            sql = """
                    SELECT id, title, author, isbn, genre,
                           total_copies, available_copies
                    FROM books
                    WHERE title LIKE ?
                       OR author LIKE ?
                       OR genre LIKE ?
                       OR isbn LIKE ?
                    ORDER BY title
                    """;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (search != null && !search.trim().isEmpty()) {

                String keyword = "%" + search.trim() + "%";

                statement.setString(1, keyword);
                statement.setString(2, keyword);
                statement.setString(3, keyword);
                statement.setString(4, keyword);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    books.add(new Book(
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getString("isbn"),
                            resultSet.getString("genre"),
                            resultSet.getInt("total_copies"),
                            resultSet.getInt("available_copies")
                    ));
                }
            }

            request.setAttribute("books", books);
            request.setAttribute("search", search == null ? "" : search);

            request.getRequestDispatcher("books.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load books."
            );
        }
    }
}