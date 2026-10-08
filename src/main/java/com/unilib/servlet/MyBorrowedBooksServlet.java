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

@WebServlet("/my-books")
public class MyBorrowedBooksServlet extends HttpServlet {

    public static class BorrowedBook {

        private int transactionId;
        private String title;
        private String author;
        private String borrowDate;
        private String dueDate;
        private String status;

        public BorrowedBook(int transactionId,
                            String title,
                            String author,
                            String borrowDate,
                            String dueDate,
                            String status) {

            this.transactionId = transactionId;
            this.title = title;
            this.author = author;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
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

        List<BorrowedBook> borrowedBooks = new ArrayList<>();

        String sql = """
                SELECT
                    t.id,
                    b.title,
                    b.author,
                    t.borrow_date,
                    t.due_date,
                    t.status
                FROM transactions t
                JOIN books b ON t.book_id = b.id
                WHERE t.member_id = ?
                  AND t.status = 'BORROWED'
                ORDER BY t.due_date
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    borrowedBooks.add(
                            new BorrowedBook(
                                    resultSet.getInt("id"),
                                    resultSet.getString("title"),
                                    resultSet.getString("author"),
                                    resultSet.getDate("borrow_date").toString(),
                                    resultSet.getDate("due_date").toString(),
                                    resultSet.getString("status")
                            )
                    );
                }
            }

            request.setAttribute("borrowedBooks", borrowedBooks);

            request.getRequestDispatcher("my-books.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load borrowed books."
            );
        }
    }
}