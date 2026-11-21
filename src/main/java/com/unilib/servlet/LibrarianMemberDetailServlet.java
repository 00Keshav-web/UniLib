package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/librarian/member")
public class LibrarianMemberDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can view member details.");
            return;
        }

        int memberId;
        try {
            memberId = Integer.parseInt(request.getParameter("id"));
            if (memberId <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/librarian/members?error=invalid");
            return;
        }

        String memberSql = """
                SELECT id, name, email, membership_id, created_at
                FROM users
                WHERE id = ? AND role = 'MEMBER'
                """;
        String historySql = """
                SELECT t.id, b.title, b.author, t.borrow_date, t.due_date,
                       t.return_date, t.status
                FROM transactions t
                JOIN books b ON b.id = t.book_id
                WHERE t.member_id = ?
                ORDER BY t.borrow_date DESC, t.id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement memberStatement = connection.prepareStatement(memberSql)) {
            memberStatement.setInt(1, memberId);
            try (ResultSet rs = memberStatement.executeQuery()) {
                if (!rs.next()) {
                    response.sendRedirect(request.getContextPath() + "/librarian/members?error=notfound");
                    return;
                }
                request.setAttribute("memberId", rs.getInt("id"));
                request.setAttribute("memberName", rs.getString("name"));
                request.setAttribute("memberEmail", rs.getString("email"));
                request.setAttribute("membershipId", rs.getString("membership_id"));
                Timestamp joined = rs.getTimestamp("created_at");
                request.setAttribute("joinedDate", joined == null ? "" : joined.toLocalDateTime().toLocalDate().toString());
            }

            List<Loan> history = new ArrayList<>();
            try (PreparedStatement historyStatement = connection.prepareStatement(historySql)) {
                historyStatement.setInt(1, memberId);
                try (ResultSet rs = historyStatement.executeQuery()) {
                    while (rs.next()) {
                        Date returned = rs.getDate("return_date");
                        history.add(new Loan(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getDate("borrow_date").toString(),
                                rs.getDate("due_date").toString(),
                                returned == null ? "—" : returned.toString(),
                                rs.getString("status")
                        ));
                    }
                }
            }
            request.setAttribute("history", history);
            request.getRequestDispatcher("/librarian-member-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Unable to load member details.", e);
        }
    }

    public static class Loan {
        private final int transactionId;
        private final String title, author, borrowDate, dueDate, returnDate, status;

        public Loan(int transactionId, String title, String author, String borrowDate,
                    String dueDate, String returnDate, String status) {
            this.transactionId = transactionId;
            this.title = title;
            this.author = author;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
        }
        public int getTransactionId() { return transactionId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getBorrowDate() { return borrowDate; }
        public String getDueDate() { return dueDate; }
        public String getReturnDate() { return returnDate; }
        public String getStatus() { return status; }
    }
}