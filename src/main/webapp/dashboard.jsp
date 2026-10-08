<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String userName = (String) session.getAttribute("userName");
    String email = (String) session.getAttribute("userEmail");
    String membershipId = (String) session.getAttribute("membershipId");
    String role = (String) session.getAttribute("role");
%>

<!DOCTYPE html>

<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Dashboard | UniLib</title>

    <link rel="stylesheet" href="css/style.css">

</head>

<body>

<header class="dashboard-header">

    <div class="dashboard-logo">
        📚 UniLib
    </div>

    <div>

        <span>
            Welcome, <strong><%= userName %></strong>
        </span>

        &nbsp;&nbsp;

        <a href="logout">Logout</a>

    </div>

</header>


<main class="dashboard-container">

    <div class="welcome-card">

        <h1>
            Welcome to UniLib 👋
        </h1>

        <p>
            Manage your books, borrowing history and library account
            from one place.
        </p>

    </div>


    <div class="profile-card">

        <h2>Your Profile</h2>

        <p>
            <strong>Name:</strong>
            <%= userName %>
        </p>

        <p>
            <strong>Email:</strong>
            <%= email %>
        </p>

        <p>
            <strong>Membership ID:</strong>
            <%= membershipId %>
        </p>

        <p>
            <strong>Role:</strong>
            <%= role %>
        </p>

    </div>


    <div class="dashboard-grid">

        <a href="books" class="dashboard-option dashboard-link">
    <h3>📚 Browse Books</h3>
    <p>Search and explore available books.</p>
</a>

        <div class="dashboard-option">
            <a href="my-books" class="dashboard-option">
                <h3>📖 My Borrowed Books</h3>
                <p>View books currently borrowed.</p>
            </a>
        </div>

        <div class="dashboard-option">
            <h3>🔄 Borrowing History</h3>
            <p>View your previous transactions.</p>
        </div>

        <div class="dashboard-option">
            <h3>🔔 Notifications</h3>
            <p>Check due dates and library updates.</p>
        </div>

    </div>

</main>

</body>
</html>