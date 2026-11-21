package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/notification-read")
public class MarkNotificationReadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String notificationId = request.getParameter("notificationId");

        String sql;
        try (Connection connection = DatabaseConnection.getConnection()) {
            if ("all".equals(notificationId)) {
                sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }
            } else {
                int id;
                try {
                    id = Integer.parseInt(notificationId);
                    if (id <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid notification.");
                    return;
                }

                sql = "UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, id);
                    statement.setInt(2, userId);
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new ServletException("Unable to update notification.", e);
        }

        response.sendRedirect(request.getContextPath() + "/notifications");
    }
}
