
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.Connection, java.sql.PreparedStatement, java.sql.ResultSet" %>
<%@ page import="com.unilib.database.DatabaseConnection" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String role = (String) session.getAttribute("role");

    if (!"LIBRARIAN".equals(role)) {
        response.sendRedirect("dashboard.jsp");
        return;
    }

    int totalBooks = 0;
    int totalMembers = 0;
    int activeLoans = 0;
    int overdueLoans = 0;

    try (Connection connection = DatabaseConnection.getConnection()) {

        String booksQuery = "SELECT COALESCE(SUM(total_copies), 0) FROM books";
        try (PreparedStatement ps = connection.prepareStatement(booksQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalBooks = rs.getInt(1);
            }
        }

        String membersQuery =
            "SELECT COUNT(*) FROM users WHERE role = 'MEMBER'";
        try (PreparedStatement ps = connection.prepareStatement(membersQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalMembers = rs.getInt(1);
            }
        }

        String activeLoansQuery =
            "SELECT COUNT(*) FROM transactions WHERE status = 'BORROWED'";
        try (PreparedStatement ps = connection.prepareStatement(activeLoansQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                activeLoans = rs.getInt(1);
            }
        }

        String overdueLoansQuery =
            "SELECT COUNT(*) FROM transactions " +
            "WHERE status = 'BORROWED' AND due_date < CURDATE()";
        try (PreparedStatement ps = connection.prepareStatement(overdueLoansQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                overdueLoans = rs.getInt(1);
            }
        }

    } catch (Exception e) {
        application.log("Unable to load librarian dashboard statistics", e);
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Librarian Dashboard | UniLib</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

<header class="dashboard-header">
    <div class="dashboard-logo">UniLib | Librarian</div>

    <div>
        Welcome, <strong><%= session.getAttribute("userName") %></strong>
        &nbsp;&nbsp;
        <a href="logout">Logout</a>
    </div>
</header>

<main class="dashboard-container">

    <div class="welcome-card">
        <h1>Librarian Dashboard</h1>
        <p>Manage books, members, and library borrowing activity.</p>
    </div>

    <div class="dashboard-grid">

        <div class="dashboard-option">
            <h3>Total Book Copies</h3>
            <p><%= totalBooks %></p>
        </div>

        <div class="dashboard-option">
            <h3>Registered Members</h3>
            <p><%= totalMembers %></p>
        </div>

        <div class="dashboard-option">
            <h3>Active Loans</h3>
            <p><%= activeLoans %></p>
        </div>

        <div class="dashboard-option">
            <h3>Overdue Loans</h3>
            <p><%= overdueLoans %></p>
        </div>

    </div>

    <div class="profile-card">
        <h2>Library Management</h2>
        <p>Next, we'll connect these options to real librarian functions.</p>

        <div class="dashboard-grid">
            <a href="librarian/books" class="dashboard-option">
                <h3>Manage Books</h3>
                <p>Add, edit, and remove books from the catalogue.</p>
            </a>

            <a href="librarian-members.jsp" class="dashboard-option">
                <h3>Manage Members</h3>
                <p>View registered library members.</p>
            </a>

            <a href="librarian-loans.jsp" class="dashboard-option">
                <h3>Manage Loans</h3>
                <p>Review borrowed, returned, and overdue books.</p>
            </a>
        </div>
    </div>

</main>

</body>
</html>
