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

@WebServlet("/librarian-dashboard")
public class LibrarianDashboardServlet extends HttpServlet {

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
                    "Librarian access required.");
            return;
        }

        String booksSql =
                "SELECT COALESCE(SUM(total_copies), 0) FROM books";
        String membersSql =
                "SELECT COUNT(*) FROM users WHERE role = 'MEMBER'";
        String activeLoansSql =
                "SELECT COUNT(*) FROM transactions WHERE status = 'BORROWED'";
        String overdueLoansSql =
                "SELECT COUNT(*) FROM transactions " +
                "WHERE status = 'BORROWED' AND due_date < CURDATE()";

        try (Connection connection = DatabaseConnection.getConnection()) {

            request.setAttribute("totalBooks",
                    getCount(connection, booksSql));
            request.setAttribute("totalMembers",
                    getCount(connection, membersSql));
            request.setAttribute("activeLoans",
                    getCount(connection, activeLoansSql));
            request.setAttribute("overdueLoans",
                    getCount(connection, overdueLoansSql));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load librarian dashboard statistics.", e);
        }

        request.getRequestDispatcher("/librarian-dashboard.jsp")
               .forward(request, response);
    }

    private int getCount(Connection connection, String sql)
            throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }

        return 0;
    }
}
