package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/librarian/record-fine-payment")
public class RecordFinePaymentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String base = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(base + "/login.jsp"); return;
        }
        if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Librarian access required."); return;
        }

        int fineId;
        try {
            fineId = Integer.parseInt(request.getParameter("fineId"));
            if (fineId <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            response.sendRedirect(base + "/librarian/fines?error=invalid"); return;
        }

        int librarianId = (Integer) session.getAttribute("userId");
        String sql = """
                UPDATE fines
                SET payment_status = 'PAID', paid_at = CURRENT_TIMESTAMP, recorded_by = ?
                WHERE id = ? AND payment_status = 'UNPAID'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, librarianId);
            statement.setInt(2, fineId);
            int updated = statement.executeUpdate();
            response.sendRedirect(base + "/librarian/fines?" +
                    (updated == 1 ? "success=paid" : "error=already"));
        } catch (SQLException e) {
            throw new ServletException("Unable to record fine payment.", e);
        }
    }
}