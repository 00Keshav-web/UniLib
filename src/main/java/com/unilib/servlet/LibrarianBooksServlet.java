
package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/librarian/books")
public class LibrarianBooksServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only librarians can manage books.");
            return;
        }

        List<Book> books = new ArrayList<>();

        String sql = "SELECT id, title, author, isbn, genre, " +
                     "total_copies, available_copies " +
                     "FROM books ORDER BY title";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Book book = new Book();

                book.setId(resultSet.getInt("id"));
                book.setTitle(resultSet.getString("title"));
                book.setAuthor(resultSet.getString("author"));
                book.setIsbn(resultSet.getString("isbn"));
                book.setGenre(resultSet.getString("genre"));
                book.setTotalCopies(resultSet.getInt("total_copies"));
                book.setAvailableCopies(
                        resultSet.getInt("available_copies"));

                books.add(book);
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the book catalogue.", e);
        }

        request.setAttribute("books", books);

        request.getRequestDispatcher("/librarian-books.jsp")
               .forward(request, response);
    }

    public static class Book {
        private int id;
        private String title;
        private String author;
        private String isbn;
        private String genre;
        private int totalCopies;
        private int availableCopies;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public int getTotalCopies() { return totalCopies; }
        public void setTotalCopies(int totalCopies) {
            this.totalCopies = totalCopies;
        }

        public int getAvailableCopies() { return availableCopies; }
        public void setAvailableCopies(int availableCopies) {
            this.availableCopies = availableCopies;
        }
    }
}