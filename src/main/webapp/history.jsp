<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.BorrowingHistoryServlet.HistoryRecord" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<HistoryRecord> history =
            (List<HistoryRecord>) request.getAttribute("history");

    if (history == null) {
        response.sendRedirect("history");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Borrowing History | UniLib</title>

    <link rel="stylesheet" href="css/style.css">

</head>

<body>

<header class="dashboard-header">

    <div class="dashboard-logo">
        UniLib
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

            <h1>Borrowing History</h1>

            <p>
                View your complete library borrowing history.
            </p>

        </div>

    </div>


    <div class="books-count">

        <strong>
            <%= history.size() %>
        </strong>

        transaction(s)

    </div>


    <% if (history.isEmpty()) { %>

        <div class="no-books">

            <h2>No borrowing history</h2>

            <p>
                You haven't borrowed any books yet.
            </p>

            <br>

            <a href="books">
                Browse Books
            </a>

        </div>

    <% } else { %>


        <div class="books-grid">

            <% for (HistoryRecord record : history) { %>

                <div class="book-card">

                    <div class="book-card-icon">
                        Book
                    </div>

                    <div class="book-card-content">

                        <h2>
                            <%= record.getTitle() %>
                        </h2>

                        <p class="book-author">
                            by <%= record.getAuthor() %>
                        </p>


                        <span class="book-genre">
                            <%= record.getStatus() %>
                        </span>


                        <div class="book-availability">

                            <span>
                                Borrowed:
                                <strong>
                                    <%= record.getBorrowDate() %>
                                </strong>
                            </span>

                            <span>
                                Due:
                                <strong>
                                    <%= record.getDueDate() %>
                                </strong>
                            </span>

                        </div>


                        <p class="book-isbn">

                            Returned:

                            <strong>
                                <%
                                    if (record.getReturnDate() == null) {
                                %>
                                    Not returned
                                <%
                                    } else {
                                %>
                                    <%= record.getReturnDate() %>
                                <%
                                    }
                                %>
                            </strong>

                        </p>

                    </div>

                </div>

            <% } %>

        </div>

    <% } %>


</main>

</body>

</html>