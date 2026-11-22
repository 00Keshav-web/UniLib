<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.LibrarianMemberDetailServlet.Loan" %>
<%!
    private String h(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
%>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp"); return;
    }
    if (!"LIBRARIAN".equals(session.getAttribute("role"))) {
        response.sendError(403, "Librarian access required."); return;
    }
    List<Loan> history = (List<Loan>) request.getAttribute("history");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Member Details | UniLib</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

</head>
<body>
<header class="dashboard-header">
 <div class="dashboard-logo">UniLib | Member Details</div>
 <div><a href="<%= request.getContextPath() %>/members">All Members</a>&nbsp;&nbsp;<a href="<%= request.getContextPath() %>/logout">Logout</a></div>
</header>
<main class="detail-container">
 <a href="<%= request.getContextPath() %>/members" style="color:#24466f;font-weight:600">← Back to Members</a>
 <h1 style="margin-top:18px">Member Profile</h1>
 <section class="detail-card">
  <div class="detail-grid">
   <div><span class="detail-label">Full Name</span><span class="detail-value"><%= h((String) request.getAttribute("memberName")) %></span></div>
   <div><span class="detail-label">Email</span><span class="detail-value"><%= h((String) request.getAttribute("memberEmail")) %></span></div>
   <div><span class="detail-label">Membership ID</span><span class="detail-value"><%= h((String) request.getAttribute("membershipId")) %></span></div>
   <div><span class="detail-label">Member Since</span><span class="detail-value"><%= h((String) request.getAttribute("joinedDate")) %></span></div>
  </div>
 </section>
 <h2>Borrowing History</h2>
 <p style="color:#6b7280;margin:8px 0 18px">All recorded loans for this member.</p>
 <div class="history-wrap">
 <table class="history-table">
  <thead><tr><th>Transaction</th><th>Book</th><th>Author</th><th>Borrowed</th><th>Due Date</th><th>Returned</th><th>Status</th></tr></thead>
  <tbody>
  <% if (history != null && !history.isEmpty()) { for (Loan loan : history) { %>
   <tr>
    <td>#<%= loan.getTransactionId() %></td>
    <td><strong><%= h(loan.getTitle()) %></strong></td>
    <td><%= h(loan.getAuthor()) %></td>
    <td><%= h(loan.getBorrowDate()) %></td>
    <td><%= h(loan.getDueDate()) %></td>
    <td><%= h(loan.getReturnDate()) %></td>
    <td class="status"><%= h(loan.getStatus()) %></td>
   </tr>
  <% } } else { %>
   <tr><td colspan="7" class="empty-history">This member has no borrowing records yet.</td></tr>
  <% } %>
  </tbody>
 </table>
 </div>
</main>
</body>
</html>