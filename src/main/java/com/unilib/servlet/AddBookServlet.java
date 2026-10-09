
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
import java.sql.SQLException;

@WebServlet("/librarian/add-book")
public class AddBookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only librarians can add books.");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String isbn = request.getParameter("isbn");
        String genre = request.getParameter("genre");
        String copiesText = request.getParameter("totalCopies");

        if (title == null || author == null || isbn == null || genre == null
                || copiesText == null
                || title.trim().isEmpty()
                || author.trim().isEmpty()
                || isbn.trim().isEmpty()
                || genre.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/librarian/books?error=missing");
            return;
        }

        title = title.trim();
        author = author.trim();
        isbn = isbn.trim();
        genre = genre.trim();

        if (title.length() > 200 || author.length() > 150
                || isbn.length() > 30 || genre.length() > 100) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/librarian/books?error=length");
            return;
        }

        int totalCopies;

        try {
            totalCopies = Integer.parseInt(copiesText.trim());

            if (totalCopies < 1 || totalCopies > 10000) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/librarian/books?error=copies");
            return;
        }

        String sql = "INSERT INTO books "
                + "(title, author, isbn, genre, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, title);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.setString(4, genre);
            statement.setInt(5, totalCopies);
            statement.setInt(6, totalCopies);

            statement.executeUpdate();

            response.sendRedirect(
                    request.getContextPath()
                    + "/librarian/books?success=added");

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/librarian/books?error=duplicate");
                return;
            }

            throw new ServletException("Unable to add book.", e);
        }
    }
}