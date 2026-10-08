package com.unilib.servlet;

import com.unilib.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate empty fields
        if (name == null || email == null || password == null ||
                confirmPassword == null ||
                name.trim().isEmpty() ||
                email.trim().isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty()) {

            response.sendRedirect(
                    "register.jsp?error=Please+fill+all+fields"
            );
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            response.sendRedirect(
                    "register.jsp?error=Passwords+do+not+match"
            );
            return;
        }

        String checkEmailSql =
                "SELECT id FROM users WHERE email = ?";

        String insertSql = """
                INSERT INTO users
                (name, email, password, membership_id, role)
                VALUES (?, ?, ?, ?, 'MEMBER')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkEmailSql)) {

            // Check if email already exists
            checkStatement.setString(1, email.trim());

            try (ResultSet resultSet =
                         checkStatement.executeQuery()) {

                if (resultSet.next()) {
                    response.sendRedirect(
                            "register.jsp?error=Email+already+registered"
                    );
                    return;
                }
            }

            // Generate membership ID
            String membershipId =
                    "LIB-" + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertSql)) {

                insertStatement.setString(1, name.trim());
                insertStatement.setString(2, email.trim());
                insertStatement.setString(3, password);
                insertStatement.setString(4, membershipId);

                insertStatement.executeUpdate();
            }

            response.sendRedirect(
                    "login.jsp?success=Account+created+successfully"
            );

        } catch (Exception e) {
            e.printStackTrace();

            response.sendRedirect(
                    "register.jsp?error=Registration+failed"
            );
        }
    }
}