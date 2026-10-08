<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.MyBorrowedBooksServlet.BorrowedBook" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<BorrowedBook> borrowedBooks =
            (List<BorrowedBook>) request.getAttribute("borrowedBooks");

    if (borrowedBooks == null) {
        response.sendRedirect("my-books");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>My Borrowed Books | UniLib</title>

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

            <h1>My Borrowed Books 📖</h1>

            <p>
                View the books you currently have borrowed.
            </p>

        </div>

    </div>


    <% if (success != null) { %>

        <div class="message success-message">
            <%= success %>
        </div>

    <% } %>


    <% if (error != null) { %>

        <div class="message error-message">
            <%= error %>
        </div>

    <% } %>


    <div class="books-count">

        <strong>
            <%= borrowedBooks.size() %>
        </strong>

        currently borrowed

    </div>


    <% if (borrowedBooks.isEmpty()) { %>

        <div class="no-books">

            <h2>No borrowed books 📚</h2>

            <p>
                You currently don't have any borrowed books.
            </p>

            <br>

            <a href="books">
                Browse Books
            </a>

        </div>

    <% } else { %>


        <div class="books-grid">

            <% for (BorrowedBook book : borrowedBooks) { %>

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
                            <%= book.getStatus() %>
                        </span>

                        <div class="book-availability">

                            <span>
                                Borrowed:
                                <strong>
                                    <%= book.getBorrowDate() %>
                                </strong>
                            </span>

                            <span>
                                Due:
                                <strong>
                                    <%= book.getDueDate() %>
                                </strong>
                            </span>

                        </div>


                        <form action="return-book" method="post">

                          <input type="hidden"
                                 name="transactionId"
                                 value="<%= book.getTransactionId() %>">
                      
                          <button class="borrow-button"
                                  type="submit">
                              Return Book
                          </button>
                      
                      </form>

                    </div>

                </div>

            <% } %>

        </div>

    <% } %>


</main>

</body>

</html>