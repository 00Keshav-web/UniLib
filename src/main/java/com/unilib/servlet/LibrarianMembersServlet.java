package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/librarian/members")
public class LibrarianMembersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only librarians can manage members.");
            return;
        }

        String search = request.getParameter("search");
        if (search == null) search = "";
        search = search.trim();

        List<Member> members = new ArrayList<>();
        String sql = """
                SELECT u.id, u.name, u.email, u.membership_id, u.created_at,
                       COUNT(t.id) AS total_loans,
                       SUM(CASE WHEN t.status = 'BORROWED' THEN 1 ELSE 0 END) AS active_loans
                FROM users u
                LEFT JOIN transactions t ON t.member_id = u.id
                WHERE u.role = 'MEMBER'
                  AND (? = '' OR u.name LIKE ? OR u.email LIKE ? OR u.membership_id LIKE ?)
                GROUP BY u.id, u.name, u.email, u.membership_id, u.created_at
                ORDER BY u.name
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + search + "%";
            statement.setString(1, search);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    members.add(new Member(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("membership_id"),
                            rs.getTimestamp("created_at") == null ? "" : rs.getTimestamp("created_at").toLocalDateTime().toLocalDate().toString(),
                            rs.getInt("total_loans"),
                            rs.getInt("active_loans")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Unable to load member directory.", e);
        }

        request.setAttribute("members", members);
        request.setAttribute("search", search);
        request.getRequestDispatcher("/librarian-members.jsp").forward(request, response);
    }

    public static class Member {
        private final int id;
        private final String name, email, membershipId, joinedDate;
        private final int totalLoans, activeLoans;

        public Member(int id, String name, String email, String membershipId,
                      String joinedDate, int totalLoans, int activeLoans) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.membershipId = membershipId;
            this.joinedDate = joinedDate;
            this.totalLoans = totalLoans;
            this.activeLoans = activeLoans;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getMembershipId() { return membershipId; }
        public String getJoinedDate() { return joinedDate; }
        public int getTotalLoans() { return totalLoans; }
        public int getActiveLoans() { return activeLoans; }
    }
}