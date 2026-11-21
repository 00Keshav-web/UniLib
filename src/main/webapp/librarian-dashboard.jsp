<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
        response.sendError(403, "Librarian access required.");
        return;
    }

    Integer totalBooks = (Integer) request.getAttribute("totalBooks");
    Integer totalMembers = (Integer) request.getAttribute("totalMembers");
    Integer activeLoans = (Integer) request.getAttribute("activeLoans");
    Integer overdueLoans = (Integer) request.getAttribute("overdueLoans");

    if (totalBooks == null || totalMembers == null ||
            activeLoans == null || overdueLoans == null) {
        response.sendRedirect("librarian-dashboard");
        return;
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
        <p>Choose a management area to continue.</p>

        <div class="dashboard-grid">
            <a href="librarian/books" class="dashboard-option">
                <h3>Manage Books</h3>
                <p>View the catalogue and add new books.</p>
            </a>

            <a href="librarian/members" class="dashboard-option">
                <h3>Manage Members</h3>
                <p>View registered members, search accounts, and inspect borrowing history.</p>
            </a>

            <a href="librarian/loans" class="dashboard-option">
                <h3>Manage Loans</h3>
                <p>Review active, overdue and returned loans, and record book returns.</p>
            </a>

            <a href="librarian/fines" class="dashboard-option">
                <h3>Manage Fines</h3>
                <p>Review accruing and finalized fines, and record received payments.</p>
            </a>
        </div>
    </div>

</main>

</body>
</html>
