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

@WebServlet("/borrow")
public class BorrowBookServlet extends HttpServlet {

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

        String bookIdParameter = request.getParameter("bookId");

        if (bookIdParameter == null || bookIdParameter.trim().isEmpty()) {
            response.sendRedirect("books?error=Invalid+book");
            return;
        }

        int bookId;

        try {
            bookId = Integer.parseInt(bookIdParameter);
        } catch (NumberFormatException e) {
            response.sendRedirect("books?error=Invalid+book");
            return;
        }

        int memberId = (Integer) session.getAttribute("userId");

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();

            // Start database transaction
            connection.setAutoCommit(false);

            // Check book availability and lock the row
            String checkBookSql = """
                    SELECT available_copies
                    FROM books
                    WHERE id = ?
                    FOR UPDATE
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(checkBookSql)) {

                statement.setInt(1, bookId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        connection.rollback();
                        response.sendRedirect("books?error=Book+not+found");
                        return;
                    }

                    int availableCopies =
                            resultSet.getInt("available_copies");

                    if (availableCopies <= 0) {
                        connection.rollback();
                        response.sendRedirect(
                                "books?error=Book+is+currently+unavailable");
                        return;
                    }
                }
            }

            // Check whether this member already has this book
            String checkBorrowedSql = """
                    SELECT id
                    FROM transactions
                    WHERE book_id = ?
                      AND member_id = ?
                      AND status = 'BORROWED'
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(checkBorrowedSql)) {

                statement.setInt(1, bookId);
                statement.setInt(2, memberId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        connection.rollback();
                        response.sendRedirect(
                                "books?error=You+already+borrowed+this+book");
                        return;
                    }
                }
            }

            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(14);

// Get the book title for the notification
String bookTitle = "";

String bookTitleSql = "SELECT title FROM books WHERE id = ?";

try (PreparedStatement statement =
             connection.prepareStatement(bookTitleSql)) {

    statement.setInt(1, bookId);

    try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
            bookTitle = resultSet.getString("title");
        }
    }
}

            // Create transaction record
            String insertTransactionSql = """
                    INSERT INTO transactions
                    (book_id, member_id, borrow_date, due_date, return_date, status)
                    VALUES (?, ?, ?, ?, NULL, 'BORROWED')
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(insertTransactionSql)) {

                statement.setInt(1, bookId);
                statement.setInt(2, memberId);
                statement.setDate(3, Date.valueOf(borrowDate));
                statement.setDate(4, Date.valueOf(dueDate));

                statement.executeUpdate();
            }

            // Decrease available copies
            String updateBookSql = """
                    UPDATE books
                    SET available_copies = available_copies - 1
                    WHERE id = ?
                      AND available_copies > 0
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(updateBookSql)) {

                statement.setInt(1, bookId);

                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated != 1) {
                    connection.rollback();
                    response.sendRedirect(
                            "books?error=Unable+to+update+book+availability");
                    return;
                }
            }

            // Everything succeeded
            connection.commit();

		// Create a notification for the member
String notificationSql = """
        INSERT INTO notifications
        (user_id, title, message, type, is_read)
        VALUES (?, ?, ?, ?, 0)
        """;

try (PreparedStatement statement =
             connection.prepareStatement(notificationSql)) {

    statement.setInt(1, memberId);
    statement.setString(2, "Book Borrowed Successfully");
    statement.setString(
            3,
            "You borrowed \"" + bookTitle
                    + "\". Please return it by " + dueDate + "."
    );
    statement.setString(4, "BORROW");

    statement.executeUpdate();
}

            response.sendRedirect(
                    "books?success=Book+borrowed+successfully");

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
                    "books?error=Unable+to+borrow+book");

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