package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

@WebServlet("/librarian/delete-book")
public class DeleteBookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can delete books.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
            if (id < 1) throw new NumberFormatException();
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/librarian/books?error=invalid");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT id FROM books WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, id);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            response.sendRedirect(request.getContextPath() + "/librarian/books?error=notfound");
                            return;
                        }
                    }
                }

                try (PreparedStatement history = connection.prepareStatement(
                        "SELECT COUNT(*) FROM transactions WHERE book_id = ?")) {
                    history.setInt(1, id);
                    try (ResultSet rs = history.executeQuery()) {
                        rs.next();
                        if (rs.getLong(1) > 0) {
                            connection.rollback();
                            response.sendRedirect(request.getContextPath() + "/librarian/books?error=hastransactions");
                            return;
                        }
                    }
                }

                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM books WHERE id = ?")) {
                    delete.setInt(1, id);
                    delete.executeUpdate();
                }

                connection.commit();
                response.sendRedirect(request.getContextPath() + "/librarian/books?success=deleted");
            } catch (SQLException e) {
                connection.rollback();
                if ("23000".equals(e.getSQLState())) {
                    response.sendRedirect(request.getContextPath() + "/librarian/books?error=hastransactions");
                    return;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to delete book.", e);
        }
    }
}
