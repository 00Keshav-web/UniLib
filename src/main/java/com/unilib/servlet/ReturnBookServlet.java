package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;

@WebServlet("/return-book")
public class ReturnBookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // User must be logged in
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String transactionIdParameter =
                request.getParameter("transactionId");

        if (transactionIdParameter == null ||
                transactionIdParameter.trim().isEmpty()) {

            response.sendRedirect(
                    "my-books?error=Invalid+transaction");

            return;
        }

        int transactionId;

        try {
            transactionId =
                    Integer.parseInt(transactionIdParameter);

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    "my-books?error=Invalid+transaction");

            return;
        }

        int memberId = (Integer) session.getAttribute("userId");

        Connection connection = null;

        try {

            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            /*
             * Find the active transaction belonging to
             * the logged-in member.
             */
            String findTransactionSql = """
                    SELECT book_id
                    FROM transactions
                    WHERE id = ?
                      AND member_id = ?
                      AND status = 'BORROWED'
                    FOR UPDATE
                    """;

            int bookId;
	    String bookTitle;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 findTransactionSql)) {

                statement.setInt(1, transactionId);
                statement.setInt(2, memberId);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (!resultSet.next()) {

                        connection.rollback();

                        response.sendRedirect(
                                "my-books?error=Borrowed+book+not+found");

                        return;
                    }

                    bookId = resultSet.getInt("book_id");
		    String titleSql = "SELECT title FROM books WHERE id = ?";

try (PreparedStatement titleStatement =
             connection.prepareStatement(titleSql)) {

    titleStatement.setInt(1, bookId);

    try (ResultSet titleResult = titleStatement.executeQuery()) {
        if (titleResult.next()) {
            bookTitle = titleResult.getString("title");
        } else {
            connection.rollback();
            response.sendRedirect(
                    "my-books?error=Book+not+found");
            return;
        }
    }
}
                }
            }

            /*
             * Mark the transaction as returned.
             */
            String updateTransactionSql = """
                    UPDATE transactions
                    SET return_date = ?,
                        status = 'RETURNED'
                    WHERE id = ?
                      AND member_id = ?
                      AND status = 'BORROWED'
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 updateTransactionSql)) {

                statement.setDate(
                        1,
                        Date.valueOf(LocalDate.now()));

                statement.setInt(2, transactionId);
                statement.setInt(3, memberId);

                int rowsUpdated =
                        statement.executeUpdate();

                if (rowsUpdated != 1) {

                    connection.rollback();

                    response.sendRedirect(
                            "my-books?error=Unable+to+return+book");

                    return;
                }
            }

            /*
             * Increase available copies.
             */
            String updateBookSql = """
                    UPDATE books
                    SET available_copies = available_copies + 1
                    WHERE id = ?
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 updateBookSql)) {

                statement.setInt(1, bookId);

                int rowsUpdated =
                        statement.executeUpdate();

                if (rowsUpdated != 1) {

                    connection.rollback();

                    response.sendRedirect(
                            "my-books?error=Unable+to+update+book+availability");

                    return;
                }
            }

            // Create a return confirmation notification
String notificationSql = """
        INSERT INTO notifications
        (user_id, title, message, type, is_read)
        VALUES (?, ?, ?, ?, 0)
        """;

try (PreparedStatement statement =
             connection.prepareStatement(notificationSql)) {

    statement.setInt(1, memberId);
    statement.setString(2, "Book Returned Successfully");
    statement.setString(
            3,
            "You returned \"" + bookTitle
                    + "\" successfully. Thank you!"
    );
    statement.setString(4, "RETURN");

    statement.executeUpdate();
}

// Everything succeeded
connection.commit();

            response.sendRedirect(
                    "my-books?success=Book+returned+successfully");

        } catch (Exception e) {

            e.printStackTrace();

            if (connection != null) {

                try {
                    connection.rollback();

                } catch (Exception rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            response.sendRedirect(
                    "my-books?error=Unable+to+return+book");

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                    connection.close();

                } catch (Exception closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }
}