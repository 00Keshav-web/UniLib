<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Login | UniLib</title>

    <link rel="stylesheet" href="css/style.css">

</head>

<body>

<div class="auth-container">

    <div class="auth-card">

        <div class="auth-logo">
            📚 UniLib
        </div>

        <h1>Welcome Back</h1>

        <p class="auth-subtitle">
            Login to access your library account
        </p>

        <% String error = request.getParameter("error"); %>

        <% if (error != null) { %>

            <div class="error-message">
                <%= error %>
            </div>

        <% } %>

        <form action="login" method="post">

            <div class="form-group">

                <label for="email">
                    Email
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="Enter your email"
                    required
                >

            </div>


            <div class="form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your password"
                    required
                >

            </div>


            <button type="submit" class="auth-button">
                Login
            </button>

        </form>

        <div class="auth-footer">

            <a href="index.jsp">
                ← Back to Home
            </a>

        </div>

    </div>

</div>

</body>
</html>