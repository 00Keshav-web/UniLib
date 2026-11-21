package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/librarian/books")
public class LibrarianBooksServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can manage books.");
            return;
        }

        String search = request.getParameter("search");
        if (search == null) search = "";
        search = search.trim();

        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, isbn, genre, total_copies, available_copies " +
                "FROM books WHERE (? = '' OR title LIKE ? OR author LIKE ? OR isbn LIKE ? OR genre LIKE ?) " +
                "ORDER BY title";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + search + "%";
            statement.setString(1, search);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setGenre(rs.getString("genre"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to load the book catalogue.", e);
        }

        request.setAttribute("books", books);
        request.setAttribute("search", search);
        request.getRequestDispatcher("/librarian-books.jsp").forward(request, response);
    }

    public static class Book {
        private int id;
        private String title, author, isbn, genre;
        private int totalCopies, availableCopies;

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
        public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
        public int getAvailableCopies() { return availableCopies; }
        public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    }
}
