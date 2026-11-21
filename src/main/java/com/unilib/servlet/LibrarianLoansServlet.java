package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/librarian/loans")
public class LibrarianLoansServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can manage loans.");
            return;
        }

        String search = request.getParameter("search");
        String status = request.getParameter("status");
        if (search == null) search = "";
        if (status == null) status = "ALL";
        search = search.trim();
        status = status.trim().toUpperCase();
        if (!List.of("ALL", "BORROWED", "RETURNED", "OVERDUE").contains(status)) status = "ALL";

        List<Loan> loans = new ArrayList<>();
        String sql = """
                SELECT t.id, t.member_id, u.name AS member_name, u.membership_id,
                       b.title, b.author, t.borrow_date, t.due_date, t.return_date,
                       CASE
                         WHEN t.status = 'BORROWED' AND t.due_date < CURDATE() THEN 'OVERDUE'
                         ELSE t.status
                       END AS display_status
                FROM transactions t
                JOIN users u ON u.id = t.member_id
                JOIN books b ON b.id = t.book_id
                WHERE u.role = 'MEMBER'
                  AND (? = '' OR u.name LIKE ? OR u.email LIKE ?
                       OR u.membership_id LIKE ? OR b.title LIKE ? OR CAST(t.id AS CHAR) LIKE ?)
                  AND (? = 'ALL' OR
                       (? = 'OVERDUE' AND t.status = 'BORROWED' AND t.due_date < CURDATE()) OR
                       (? = 'BORROWED' AND t.status = 'BORROWED' AND t.due_date >= CURDATE()) OR
                       (? = 'RETURNED' AND t.status = 'RETURNED'))
                ORDER BY
                  CASE WHEN t.status = 'BORROWED' AND t.due_date < CURDATE() THEN 0
                       WHEN t.status = 'BORROWED' THEN 1 ELSE 2 END,
                  t.due_date ASC, t.id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + search + "%";
            statement.setString(1, search);
            for (int i = 2; i <= 6; i++) statement.setString(i, pattern);
            statement.setString(7, status);
            statement.setString(8, status);
            statement.setString(9, status);
            statement.setString(10, status);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Date returned = rs.getDate("return_date");
                    loans.add(new Loan(
                            rs.getInt("id"),
                            rs.getString("member_name"),
                            rs.getString("membership_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getDate("borrow_date").toString(),
                            rs.getDate("due_date").toString(),
                            returned == null ? "—" : returned.toString(),
                            rs.getString("display_status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to load loan records.", e);
        }

        request.setAttribute("loans", loans);
        request.setAttribute("search", search);
        request.setAttribute("selectedStatus", status);
        request.getRequestDispatcher("/librarian-loans.jsp").forward(request, response);
    }

    public static class Loan {
        private final int id;
        private final String memberName, membershipId, title, author;
        private final String borrowDate, dueDate, returnDate, status;

        public Loan(int id, String memberName, String membershipId, String title, String author,
                    String borrowDate, String dueDate, String returnDate, String status) {
            this.id = id;
            this.memberName = memberName;
            this.membershipId = membershipId;
            this.title = title;
            this.author = author;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
        }
        public int getId() { return id; }
        public String getMemberName() { return memberName; }
        public String getMembershipId() { return membershipId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getBorrowDate() { return borrowDate; }
        public String getDueDate() { return dueDate; }
        public String getReturnDate() { return returnDate; }
        public String getStatus() { return status; }
    }
}