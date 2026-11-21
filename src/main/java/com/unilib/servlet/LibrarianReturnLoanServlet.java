package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@WebServlet("/librarian/return-loan")
public class LibrarianReturnLoanServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String base = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(base + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can process returns.");
            return;
        }

        int transactionId;
        try {
            transactionId = Integer.parseInt(request.getParameter("transactionId"));
            if (transactionId <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            response.sendRedirect(base + "/librarian/loans?error=invalid");
            return;
        }

        String redirect = base + "/librarian/loans";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int bookId;
                int memberId;
                Date dueDate;
                String bookTitle;
                String findSql = """
                        SELECT t.book_id, t.member_id, t.due_date, b.title
                        FROM transactions t
                        JOIN books b ON b.id = t.book_id
                        WHERE t.id = ? AND t.status = 'BORROWED'
                        FOR UPDATE
                        """;
                try (PreparedStatement statement = connection.prepareStatement(findSql)) {
                    statement.setInt(1, transactionId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            response.sendRedirect(redirect + "?error=notactive");
                            return;
                        }
                        bookId = rs.getInt("book_id");
                        memberId = rs.getInt("member_id");
                        dueDate = rs.getDate("due_date");
                        bookTitle = rs.getString("title");
                    }
                }

                String updateLoan = """
                        UPDATE transactions
                        SET status = 'RETURNED', return_date = ?
                        WHERE id = ? AND status = 'BORROWED'
                        """;
                try (PreparedStatement statement = connection.prepareStatement(updateLoan)) {
                    statement.setDate(1, Date.valueOf(LocalDate.now()));
                    statement.setInt(2, transactionId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Loan status changed before return could be completed.");
                    }
                }

                // Finalize a fine when the book is returned late.
                long overdueDays = Math.max(0, ChronoUnit.DAYS.between(
                        dueDate.toLocalDate(), LocalDate.now()));
                if (overdueDays > 0) {
                    String fineSql = """
                            INSERT INTO fines (transaction_id, member_id, amount)
                            VALUES (?, ?, ?)
                            """;
                    try (PreparedStatement fineStatement = connection.prepareStatement(fineSql)) {
                        fineStatement.setInt(1, transactionId);
                        fineStatement.setInt(2, memberId);
                        fineStatement.setBigDecimal(3,
                                java.math.BigDecimal.valueOf(overdueDays * 5L));
                        fineStatement.executeUpdate();
                    }
                }

                String updateBook = """
                        UPDATE books
                        SET available_copies = available_copies + 1
                        WHERE id = ? AND available_copies < total_copies
                        """;
                try (PreparedStatement statement = connection.prepareStatement(updateBook)) {
                    statement.setInt(1, bookId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Book availability could not be updated safely.");
                    }
                }

                String notification = """
                        INSERT INTO notifications (user_id, title, message, type, is_read)
                        VALUES (?, ?, ?, ?, 0)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(notification)) {
                    statement.setInt(1, memberId);
                    statement.setString(2, "Book Returned Successfully");
                    statement.setString(3, "The library has recorded the return of \"" + bookTitle + "\". Thank you!");
                    statement.setString(4, "RETURN");
                    statement.executeUpdate();
                }

                connection.commit();
                response.sendRedirect(redirect + "?success=returned");
            } catch (Exception e) {
                try { connection.rollback(); } catch (SQLException ignored) {}
                throw e;
            }
        } catch (Exception e) {
            throw new ServletException("Unable to process this loan return.", e);
        }
    }
}