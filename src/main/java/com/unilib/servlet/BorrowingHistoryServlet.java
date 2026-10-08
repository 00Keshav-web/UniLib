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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/history")
public class BorrowingHistoryServlet extends HttpServlet {

    public static class HistoryRecord {

        private int transactionId;
        private String title;
        private String author;
        private String borrowDate;
        private String dueDate;
        private String returnDate;
        private String status;

        public HistoryRecord(int transactionId,
                             String title,
                             String author,
                             String borrowDate,
                             String dueDate,
                             String returnDate,
                             String status) {

            this.transactionId = transactionId;
            this.title = title;
            this.author = author;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
        }

        public int getTransactionId() {
            return transactionId;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getBorrowDate() {
            return borrowDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getReturnDate() {
            return returnDate;
        }

        public String getStatus() {
            return status;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int memberId = (Integer) session.getAttribute("userId");

        List<HistoryRecord> history = new ArrayList<>();

        String sql = """
                SELECT
                    t.id,
                    b.title,
                    b.author,
                    t.borrow_date,
                    t.due_date,
                    t.return_date,
                    t.status
                FROM transactions t
                JOIN books b ON t.book_id = b.id
                WHERE t.member_id = ?
                ORDER BY t.borrow_date DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String returnDate = null;

                    if (resultSet.getDate("return_date") != null) {
                        returnDate =
                                resultSet.getDate("return_date").toString();
                    }

                    history.add(
                            new HistoryRecord(
                                    resultSet.getInt("id"),
                                    resultSet.getString("title"),
                                    resultSet.getString("author"),
                                    resultSet.getDate("borrow_date").toString(),
                                    resultSet.getDate("due_date").toString(),
                                    returnDate,
                                    resultSet.getString("status")
                            )
                    );
                }
            }

            request.setAttribute("history", history);

            request.getRequestDispatcher("history.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load borrowing history."
            );
        }
    }
}