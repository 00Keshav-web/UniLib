<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.BrowseBooksServlet.Book" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<Book> books =
            (List<Book>) request.getAttribute("books");

    String search =
            (String) request.getAttribute("search");

    if (books == null) {
        response.sendRedirect("books");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Browse Books | UniLib</title>

    <link rel="stylesheet" href="css/style.css">

</head>

<body>

<header class="dashboard-header">

    <div class="dashboard-logo">
        📚 UniLib
    </div>

    <div>
        <span>
            Welcome,
            <strong>
                <%= session.getAttribute("userName") %>
            </strong>
        </span>

        &nbsp;&nbsp;

        <a href="dashboard.jsp">Dashboard</a>

        &nbsp;&nbsp;

        <a href="logout">Logout</a>
    </div>

</header>


<main class="books-container">

    <div class="books-header">

        <div>
            <h1>Browse Books 📚</h1>

            <p>
                Search and explore books available in the library.
            </p>
        </div>

    </div>


    <form action="books" method="get" class="book-search">

        <input
            type="text"
            name="search"
            value="<%= search %>"
            placeholder="Search by title, author, genre or ISBN">

        <button type="submit">
            Search
        </button>

    </form>


    <div class="books-count">

        <strong>
            <%= books.size() %>
        </strong>

        book(s) found

    </div>


    <div class="books-grid">

        <% for (Book book : books) { %>

            <div class="book-card">

                <div class="book-card-icon">
                    📖
                </div>

                <div class="book-card-content">

                    <h2>
                        <%= book.getTitle() %>
                    </h2>

                    <p class="book-author">
                        by <%= book.getAuthor() %>
                    </p>

                    <span class="book-genre">
                        <%= book.getGenre() %>
                    </span>

                    <p class="book-isbn">
                        ISBN: <%= book.getIsbn() %>
                    </p>

                    <div class="book-availability">

                        <span>
                            Available:
                            <strong>
                                <%= book.getAvailableCopies() %>
                            </strong>
                        </span>

                        <span>
                            Total:
                            <strong>
                                <%= book.getTotalCopies() %>
                            </strong>
                        </span>

                    </div>

                    <% if (book.getAvailableCopies() > 0) { %>

                        <button class="borrow-button"
                                type="button">
                            Borrow
                        </button>

                    <% } else { %>

                        <button class="borrow-button disabled"
                                type="button"
                                disabled>
                            Not Available
                        </button>

                    <% } %>

                </div>

            </div>

        <% } %>

    </div>


    <% if (books.isEmpty()) { %>

        <div class="no-books">

            <h2>No books found 📚</h2>

            <p>
                Try searching with a different title, author,
                genre or ISBN.
            </p>

        </div>

    <% } %>


</main>

</body>
</html>