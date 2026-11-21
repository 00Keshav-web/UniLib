package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/librarian/fines")
public class LibrarianFinesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String base = request.getContextPath();
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(base + "/login.jsp"); return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Librarian access required."); return;
        }

        List<Fine> fines = new ArrayList<>();
        List<AccruingFine> accruing = new ArrayList<>();
        String fineSql = """
                SELECT f.id, f.transaction_id, f.member_id, u.name AS member_name,
                       u.membership_id, b.title, f.amount, f.payment_status,
                       f.created_at, f.paid_at, payer.name AS recorded_by_name
                FROM fines f
                JOIN users u ON u.id = f.member_id
                JOIN transactions t ON t.id = f.transaction_id
                JOIN books b ON b.id = t.book_id
                LEFT JOIN users payer ON payer.id = f.recorded_by
                ORDER BY CASE WHEN f.payment_status = 'UNPAID' THEN 0 ELSE 1 END,
                         f.created_at DESC, f.id DESC
                """;
        String accruingSql = """
                SELECT t.id AS transaction_id, u.name AS member_name, u.membership_id,
                       b.title, t.due_date, DATEDIFF(CURDATE(), t.due_date) * 5 AS amount
                FROM transactions t
                JOIN users u ON u.id = t.member_id
                JOIN books b ON b.id = t.book_id
                WHERE t.status = 'BORROWED' AND t.due_date < CURDATE()
                ORDER BY t.due_date ASC
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(fineSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp paidAt = rs.getTimestamp("paid_at");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    fines.add(new Fine(rs.getInt("id"), rs.getInt("transaction_id"),
                            rs.getString("member_name"), rs.getString("membership_id"),
                            rs.getString("title"), rs.getBigDecimal("amount"),
                            rs.getString("payment_status"),
                            createdAt == null ? "—" : createdAt.toString(),
                            paidAt == null ? "—" : paidAt.toString(),
                            rs.getString("recorded_by_name")));
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(accruingSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accruing.add(new AccruingFine(rs.getInt("transaction_id"),
                            rs.getString("member_name"), rs.getString("membership_id"),
                            rs.getString("title"), rs.getDate("due_date").toString(),
                            rs.getBigDecimal("amount")));
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to load fine records.", e);
        }
        request.setAttribute("fines", fines);
        request.setAttribute("accruingFines", accruing);
        request.getRequestDispatcher("/librarian-fines.jsp").forward(request, response);
    }

    public static class Fine {
        private final int id, transactionId;
        private final String memberName, membershipId, title, status, createdAt, paidAt, recordedBy;
        private final BigDecimal amount;
        public Fine(int id, int transactionId, String memberName, String membershipId,
                    String title, BigDecimal amount, String status, String createdAt,
                    String paidAt, String recordedBy) {
            this.id=id; this.transactionId=transactionId; this.memberName=memberName;
            this.membershipId=membershipId; this.title=title; this.amount=amount;
            this.status=status; this.createdAt=createdAt; this.paidAt=paidAt; this.recordedBy=recordedBy;
        }
        public int getId(){return id;} public int getTransactionId(){return transactionId;}
        public String getMemberName(){return memberName;} public String getMembershipId(){return membershipId;}
        public String getTitle(){return title;} public BigDecimal getAmount(){return amount;}
        public String getStatus(){return status;} public String getCreatedAt(){return createdAt;}
        public String getPaidAt(){return paidAt;} public String getRecordedBy(){return recordedBy;}
    }
    public static class AccruingFine {
        private final int transactionId;
        private final String memberName, membershipId, title, dueDate;
        private final BigDecimal amount;
        public AccruingFine(int transactionId, String memberName, String membershipId,
                            String title, String dueDate, BigDecimal amount) {
            this.transactionId=transactionId; this.memberName=memberName; this.membershipId=membershipId;
            this.title=title; this.dueDate=dueDate; this.amount=amount;
        }
        public int getTransactionId(){return transactionId;} public String getMemberName(){return memberName;}
        public String getMembershipId(){return membershipId;} public String getTitle(){return title;}
        public String getDueDate(){return dueDate;} public BigDecimal getAmount(){return amount;}
    }
}