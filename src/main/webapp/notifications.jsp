<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.NotificationsServlet.Notification" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<Notification> notifications =
            (List<Notification>) request.getAttribute("notifications");

    String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html>
<head>
    <title>Notifications - UniLib</title>
    <link rel="stylesheet" href="css/style.css">
</head>

<body>

<div class="dashboard-container">

    <div class="dashboard-header">
        <div>
            <h1>Notifications</h1>
            <p>Stay updated with your library activity.</p>
        </div>

        <a href="dashboard.jsp" class="back-button">
            Back to Dashboard
        </a>
    </div>

    <% if (error != null) { %>
        <div class="error-message">
            <%= error %>
        </div>
    <% } %>

    <% if (notifications == null || notifications.isEmpty()) { %>

        <div class="empty-state">
            <h2>No notifications</h2>
            <p>You don't have any notifications yet.</p>
        </div>

    <% } else { %>

        <div class="notifications-list">

            <% for (Notification notification : notifications) { %>

                <div class="notification-card
                    <%= notification.isRead() ? "read" : "unread" %>">

                    <div class="notification-content">

                        <h3>
                            <%= notification.getTitle() %>
                        </h3>

                        <p>
                            <%= notification.getMessage() %>
                        </p>

                        <small>
                            <%= notification.getCreatedAt() %>
                        </small>

                    </div>

                    <span class="notification-type">
                        <%= notification.getType() %>
                    </span>

                </div>

            <% } %>

        </div>

    <% } %>

</div>

</body>
</html>