
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.LibrarianBooksServlet.Book" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
        response.sendError(403, "Librarian access required.");
        return;
    }

    List<Book> books =
        (List<Book>) request.getAttribute("books");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Books | UniLib</title>
    <link rel="stylesheet" href="css/style.css">

    <style>
        .catalogue-container {
            max-width: 1200px;
            margin: 35px auto;
            padding: 24px;
        }

        .catalogue-heading {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
            flex-wrap: wrap;
            margin-bottom: 22px;
        }

        .catalogue-table-wrapper {
            overflow-x: auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.06);
        }

        .catalogue-table {
            width: 100%;
            border-collapse: collapse;
            min-width: 750px;
        }

        .catalogue-table th,
        .catalogue-table td {
            padding: 14px 16px;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }

        .catalogue-table th {
            background: #24466f;
            color: white;
        }

        .catalogue-table tr:last-child td {
            border-bottom: none;
        }

        .catalogue-table tbody tr:hover {
            background: #f7f9fc;
        }

        .back-link {
            color: #24466f;
            font-weight: 600;
            text-decoration: none;
        }

        .empty-message {
            padding: 30px;
            text-align: center;
            color: #6b7280;
        }
    </style>
</head>

<body>

<header class="dashboard-header">
    <div class="dashboard-logo">UniLib | Manage Books</div>
    <div>
        <a href="librarian-dashboard.jsp">Dashboard</a>
        &nbsp;&nbsp;
        <a href="logout">Logout</a>
    </div>
</header>

<main class="catalogue-container">

    <div class="catalogue-heading">
        <div>
            <h1>Book Catalogue</h1>
            <p>View and manage the books available in your library.</p>
        </div>

        <a href="librarian-dashboard.jsp" class="back-link">
            ← Back to Dashboard
        </a>
    </div>

    <div class="catalogue-table-wrapper">
        <table class="catalogue-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Author</th>
                    <th>ISBN</th>
                    <th>Genre</th>
                    <th>Total Copies</th>
                    <th>Available</th>
                </tr>
            </thead>

            <tbody>
                <% if (books != null && !books.isEmpty()) {
                       for (Book book : books) { %>
                    <tr>
                        <td><%= book.getId() %></td>
                        <td><%= book.getTitle() %></td>
                        <td><%= book.getAuthor() %></td>
                        <td><%= book.getIsbn() %></td>
                        <td><%= book.getGenre() %></td>
                        <td><%= book.getTotalCopies() %></td>
                        <td><%= book.getAvailableCopies() %></td>
                    </tr>
                <%     }
                   } else { %>
                    <tr>
                        <td colspan="7" class="empty-message">
                            No books found in the catalogue.
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>

</main>

</body>
</html>