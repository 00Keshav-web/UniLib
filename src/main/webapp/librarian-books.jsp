<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.LibrarianBooksServlet.Book" %>

<%!
    private String h(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
%>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
        response.sendError(403, "Librarian access required.");
        return;
    }

    List<Book> books = (List<Book>) request.getAttribute("books");
    String search = (String) request.getAttribute("search");
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Books | UniLib</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .catalogue-container{max-width:1400px;margin:35px auto;padding:24px}
        .catalogue-heading{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-bottom:22px}
        .catalogue-table-wrapper{overflow-x:auto;background:white;border-radius:12px;box-shadow:0 3px 12px rgba(0,0,0,.06)}
        .catalogue-table{width:100%;border-collapse:collapse;min-width:1050px}
        .catalogue-table th,.catalogue-table td{padding:12px;text-align:left;border-bottom:1px solid #e5e7eb;vertical-align:middle}
        .catalogue-table th{background:#24466f;color:white}
        .catalogue-table input{width:100%;min-width:90px;box-sizing:border-box;padding:8px}
        .catalogue-table .id-cell{min-width:40px}
        .row-actions{display:flex;gap:8px;align-items:center}
        .row-actions button{padding:8px 12px;cursor:pointer}
        .delete-button{background:#b91c1c;color:white;border:0;border-radius:5px}
        .search-form{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:20px}
        .search-form input{min-width:240px;flex:1}
        .notice{padding:12px 16px;margin:12px 0;border-radius:8px;background:#f0fdf4;color:#166534}
        .notice.error{background:#fef2f2;color:#991b1b}
        .empty-message{padding:30px;text-align:center;color:#6b7280}
        .back-link{color:#24466f;font-weight:600;text-decoration:none}
    </style>
</head>
<body>

<header class="dashboard-header">
    <div class="dashboard-logo">UniLib | Manage Books</div>
    <div>
        <a href="librarian-dashboard">Dashboard</a>
        &nbsp;&nbsp;
        <a href="logout">Logout</a>
    </div>
</header>

<main class="catalogue-container">
    <div class="catalogue-heading">
        <div>
            <h1>Book Catalogue</h1>
            <p>Search, add, edit and manage books in your library.</p>
        </div>
        <a href="librarian-dashboard" class="back-link">← Back to Dashboard</a>
    </div>

    <% if ("added".equals(success)) { %><div class="notice">Book added successfully.</div><% } %>
    <% if ("updated".equals(success)) { %><div class="notice">Book details updated successfully.</div><% } %>
    <% if ("deleted".equals(success)) { %><div class="notice">Book deleted successfully.</div><% } %>

    <% if ("duplicate".equals(error)) { %><div class="notice error">That ISBN is already assigned to another book.</div>
    <% } else if ("missing".equals(error) || "invalid".equals(error)) { %><div class="notice error">Please provide valid values for all fields.</div>
    <% } else if ("length".equals(error)) { %><div class="notice error">One or more fields exceed the permitted length.</div>
    <% } else if ("copies".equals(error)) { %><div class="notice error">Enter a valid copy count between 1 and 10,000.</div>
    <% } else if ("belowborrowed".equals(error)) { %><div class="notice error">Total copies cannot be less than the number currently borrowed.</div>
    <% } else if ("hastransactions".equals(error)) { %><div class="notice error">This book has borrowing history and cannot be deleted. Its history has been preserved.</div>
    <% } else if ("notfound".equals(error)) { %><div class="notice error">The selected book could not be found.</div><% } %>

    <section class="profile-card" style="margin-bottom:24px">
        <h2>Add a New Book</h2>
        <form action="add-book" method="post"
              style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:14px">
            <input type="text" name="title" placeholder="Book Title" maxlength="200" required>
            <input type="text" name="author" placeholder="Author" maxlength="150" required>
            <input type="text" name="isbn" placeholder="ISBN" maxlength="30" required>
            <input type="text" name="genre" placeholder="Genre" maxlength="100" required>
            <input type="number" name="totalCopies" placeholder="Total Copies" min="1" max="10000" required>
            <button type="submit">Add Book</button>
        </form>
    </section>

    <form class="search-form" action="books" method="get">
        <input type="search" name="search" placeholder="Search title, author, ISBN or genre"
               value="<%= h(search) %>">
        <button type="submit">Search</button>
        <a href="books" class="back-link" style="align-self:center">Clear</a>
    </form>

    <div class="catalogue-table-wrapper">
        <table class="catalogue-table">
            <thead>
                <tr>
                    <th>ID</th><th>Title</th><th>Author</th><th>ISBN</th>
                    <th>Genre</th><th>Total Copies</th><th>Available</th><th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% if (books != null && !books.isEmpty()) {
                   for (Book book : books) { %>
                <tr>
                    <form id="edit-<%= book.getId() %>" action="edit-book" method="post"></form>
                    <td class="id-cell">
                        <%= book.getId() %>
                        <input type="hidden" form="edit-<%= book.getId() %>" name="id" value="<%= book.getId() %>">
                    </td>
                    <td><input form="edit-<%= book.getId() %>" name="title" value="<%= h(book.getTitle()) %>" maxlength="200" required></td>
                    <td><input form="edit-<%= book.getId() %>" name="author" value="<%= h(book.getAuthor()) %>" maxlength="150" required></td>
                    <td><input form="edit-<%= book.getId() %>" name="isbn" value="<%= h(book.getIsbn()) %>" maxlength="30" required></td>
                    <td><input form="edit-<%= book.getId() %>" name="genre" value="<%= h(book.getGenre()) %>" maxlength="100" required></td>
                    <td><input form="edit-<%= book.getId() %>" type="number" name="totalCopies"
                               value="<%= book.getTotalCopies() %>" min="1" max="10000" required></td>
                    <td><%= book.getAvailableCopies() %></td>
                    <td>
                        <div class="row-actions">
                            <button type="submit" form="edit-<%= book.getId() %>">Save</button>
                            <form action="delete-book" method="post"
                                  onsubmit="return confirm('Delete this book? This is allowed only if it has no borrowing history.');">
                                <input type="hidden" name="id" value="<%= book.getId() %>">
                                <button type="submit" class="delete-button">Delete</button>
                            </form>
                        </div>
                    </td>
                </tr>
            <%     }
               } else { %>
                <tr><td colspan="8" class="empty-message">No books found. Try another search.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
