<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.NotificationsServlet.Notification" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    List<Notification> notifications =
            (List<Notification>) request.getAttribute("notifications");

    String error = (String) request.getAttribute("error");
    Integer unreadCountValue = (Integer) request.getAttribute("unreadCount");
    int unreadCount = unreadCountValue == null ? 0 : unreadCountValue;
%>

<!DOCTYPE html>
<html>
<head>
    <title>Notifications - UniLib</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>

<body>

<div class="dashboard-container">

    <div class="dashboard-header">
        <div>
            <h1>Notifications</h1>
            <p>Stay updated with your library activity. Unread: <strong><%= unreadCount %></strong></p>
        </div>

        <a href="<%= request.getContextPath() %>/dashboard.jsp" class="back-button" style="color: white;">
            Back to Dashboard
        </a>
    </div>

    <% if (unreadCount > 0) { %>
        <form method="post" action="<%= request.getContextPath() %>/notification-read" style="margin-bottom: 18px;">
            <input type="hidden" name="notificationId" value="all">
            <button type="submit" class="primary-button">Mark all as read</button>
        </form>
    <% } %>

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

                    <div>
                        <span class="notification-type">
                            <%= notification.getType() %>
                        </span>
                        <% if (!notification.isRead()) { %>
                            <form method="post" action="<%= request.getContextPath() %>/notification-read" style="margin-top: 10px;">
                                <input type="hidden" name="notificationId" value="<%= notification.getId() %>">
                                <button type="submit" class="secondary-button">Mark as read</button>
                            </form>
                        <% } %>
                    </div>

                </div>

            <% } %>

        </div>

    <% } %>

</div>

</body>
</html>