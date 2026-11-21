package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

@WebServlet("/librarian/edit-book")
public class EditBookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can edit books.");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        int id, totalCopies;
        try {
            id = Integer.parseInt(request.getParameter("id"));
            totalCopies = Integer.parseInt(request.getParameter("totalCopies"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/librarian/books?error=invalid");
            return;
        }

        String title = trim(request.getParameter("title"));
        String author = trim(request.getParameter("author"));
        String isbn = trim(request.getParameter("isbn"));
        String genre = trim(request.getParameter("genre"));

        if (id < 1 || totalCopies < 1 || totalCopies > 10000 ||
                title == null || title.isEmpty() || title.length() > 200 ||
                author == null || author.isEmpty() || author.length() > 150 ||
                isbn == null || isbn.isEmpty() || isbn.length() > 30 ||
                genre == null || genre.isEmpty() || genre.length() > 100) {
            response.sendRedirect(request.getContextPath() + "/librarian/books?error=invalid");
            return;
        }

        String selectSql = "SELECT total_copies, available_copies FROM books WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, " +
                "total_copies = ?, available_copies = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int borrowed;
                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            response.sendRedirect(request.getContextPath() + "/librarian/books?error=notfound");
                            return;
                        }
                        borrowed = rs.getInt("total_copies") - rs.getInt("available_copies");
                    }
                }

                if (totalCopies < borrowed) {
                    connection.rollback();
                    response.sendRedirect(request.getContextPath() + "/librarian/books?error=belowborrowed");
                    return;
                }

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setString(1, title);
                    ps.setString(2, author);
                    ps.setString(3, isbn);
                    ps.setString(4, genre);
                    ps.setInt(5, totalCopies);
                    ps.setInt(6, totalCopies - borrowed);
                    ps.setInt(7, id);
                    ps.executeUpdate();
                }

                connection.commit();
                response.sendRedirect(request.getContextPath() + "/librarian/books?success=updated");
            } catch (SQLException e) {
                connection.rollback();
                if ("23000".equals(e.getSQLState())) {
                    response.sendRedirect(request.getContextPath() + "/librarian/books?error=duplicate");
                    return;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to update book.", e);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
