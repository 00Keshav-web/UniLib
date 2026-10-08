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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty()) {

            response.sendRedirect("login.jsp?error=Please+enter+email+and+password");
            return;
        }

        String sql = """
                SELECT id, name, email, membership_id, role
                FROM users
                WHERE email = ? AND password = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    HttpSession session = request.getSession();

                    session.setAttribute("userId", resultSet.getInt("id"));
                    session.setAttribute("userName", resultSet.getString("name"));
                    session.setAttribute("userEmail", resultSet.getString("email"));
                    session.setAttribute(
                            "membershipId",
                            resultSet.getString("membership_id")
                    );
                    session.setAttribute(
                            "role",
                            resultSet.getString("role")
                    );

                    response.sendRedirect("dashboard.jsp");

                } else {

                    response.sendRedirect(
                            "login.jsp?error=Invalid+email+or+password"
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    "login.jsp?error=Database+connection+error"
            );
        }
    }
}
