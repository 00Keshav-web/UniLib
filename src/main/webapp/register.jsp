<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Register - UniLib</title>
    <link rel="stylesheet" href="css/style.css">
</head>

<body>

<div class="login-container">

    <div class="login-card">

        <h1>Create Account</h1>
        <p class="login-subtitle">Join UniLib Library</p>

        <% String error = request.getParameter("error"); %>

        <% if (error != null) { %>
            <div class="error-message">
                <%= error %>
            </div>
        <% } %>

        <form action="register" method="post">

            <label for="name">Full Name</label>
            <input
                type="text"
                id="name"
                name="name"
                placeholder="Enter your full name"
                required
            >

            <label for="email">Email</label>
            <input
                type="email"
                id="email"
                name="email"
                placeholder="Enter your email"
                required
            >

            <label for="password">Password</label>
            <input
                type="password"
                id="password"
                name="password"
                placeholder="Create a password"
                required
            >

            <label for="confirmPassword">Confirm Password</label>
            <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                placeholder="Confirm your password"
                required
            >

            <button type="submit" class="login-button">
                Create Account
            </button>

        </form>

        <p class="register-link">
            Already have an account?
            <a href="login.jsp">Login</a>
        </p>

    </div>

</div>

</body>
</html>