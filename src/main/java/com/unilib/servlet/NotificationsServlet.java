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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/notifications")
public class NotificationsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // User must be logged in
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        List<Notification> notifications = new ArrayList<>();

        String sql = """
                SELECT id, title, message, type, is_read, created_at
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Notification notification = new Notification();

                    notification.setId(resultSet.getInt("id"));
                    notification.setTitle(resultSet.getString("title"));
                    notification.setMessage(resultSet.getString("message"));
                    notification.setType(resultSet.getString("type"));
                    notification.setRead(resultSet.getBoolean("is_read"));
                    notification.setCreatedAt(
                            resultSet.getTimestamp("created_at")
                    );

                    notifications.add(notification);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(
                    "error",
                    "Unable to load notifications."
            );
        }

        request.setAttribute("notifications", notifications);

        request.getRequestDispatcher("notifications.jsp")
                .forward(request, response);
    }

    public static class Notification {

        private int id;
        private String title;
        private String message;
        private String type;
        private boolean isRead;
        private java.sql.Timestamp createdAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }

        public java.sql.Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.sql.Timestamp createdAt) {
            this.createdAt = createdAt;
        }
    }
}