<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.LibrarianLoansServlet.Loan" %>
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
    List<Loan> loans = (List<Loan>) request.getAttribute("loans");
    String search = (String) request.getAttribute("search");
    String selectedStatus = (String) request.getAttribute("selectedStatus");
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Manage Loans | UniLib</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

</head>
<body>
<header class="dashboard-header">
 <div class="dashboard-logo">UniLib | Manage Loans</div>
 <div><a href="<%= request.getContextPath() %>/librarian-dashboard">Dashboard</a>&nbsp;&nbsp;<a href="<%= request.getContextPath() %>/logout">Logout</a></div>
</header>
<main class="loan-container">
 <div class="loan-heading">
  <div><h1>Loan Management</h1><p class="loan-muted">Review borrowing transactions and process book returns.</p></div>
  <a href="<%= request.getContextPath() %>/librarian-dashboard" style="color:#24466f;font-weight:600">← Back to Dashboard</a>
 </div>
 <% if ("returned".equals(success)) { %><div class="loan-notice">Book return recorded successfully and member notified.</div><% } %>
 <% if ("invalid".equals(error)) { %><div class="loan-notice error">Invalid transaction ID.</div>
 <% } else if ("notactive".equals(error)) { %><div class="loan-notice error">This loan is no longer active or could not be found.</div><% } %>
 <form class="loan-filters" action="<%= request.getContextPath() %>/loans" method="get">
  <input type="search" name="search" placeholder="Search member, membership ID, book or transaction ID" value="<%= h(search) %>">
  <select name="status">
   <option value="ALL" <%= "ALL".equals(selectedStatus) ? "selected" : "" %>>All statuses</option>
   <option value="BORROWED" <%= "BORROWED".equals(selectedStatus) ? "selected" : "" %>>Active loans</option>
   <option value="OVERDUE" <%= "OVERDUE".equals(selectedStatus) ? "selected" : "" %>>Overdue</option>
   <option value="RETURNED" <%= "RETURNED".equals(selectedStatus) ? "selected" : "" %>>Returned</option>
  </select>
  <button type="submit">Filter</button>
  <a href="<%= request.getContextPath() %>/loans" style="align-self:center;color:#24466f;font-weight:600">Clear</a>
 </form>
 <p class="loan-muted" style="margin:12px 0">Transactions found: <strong><%= loans == null ? 0 : loans.size() %></strong></p>
 <div class="loan-table-wrap">
 <table class="loan-table">
  <thead><tr><th>Transaction</th><th>Member</th><th>Membership ID</th><th>Book</th><th>Borrowed</th><th>Due Date</th><th>Returned</th><th>Status</th><th>Action</th></tr></thead>
  <tbody>
  <% if (loans != null && !loans.isEmpty()) { for (Loan loan : loans) {
       String statusClass = "OVERDUE".equals(loan.getStatus()) ? "status-overdue" :
                            "RETURNED".equals(loan.getStatus()) ? "status-returned" : "status-borrowed";
  %>
   <tr>
    <td>#<%= loan.getId() %></td>
    <td><strong><%= h(loan.getMemberName()) %></strong></td>
    <td><%= h(loan.getMembershipId()) %></td>
    <td><%= h(loan.getTitle()) %><br><span class="loan-muted"><%= h(loan.getAuthor()) %></span></td>
    <td><%= h(loan.getBorrowDate()) %></td>
    <td><%= h(loan.getDueDate()) %></td>
    <td><%= h(loan.getReturnDate()) %></td>
    <td class="loan-status <%= statusClass %>"><%= h(loan.getStatus()) %></td>
    <td>
     <% if (!"RETURNED".equals(loan.getStatus())) { %>
      <form action="<%= request.getContextPath() %>/return-loan" method="post" onsubmit="return confirm('Confirm that this book has been returned?');">
       <input type="hidden" name="transactionId" value="<%= loan.getId() %>">
       <button class="return-button" type="submit">Record Return</button>
      </form>
     <% } else { %><span class="loan-muted">Completed</span><% } %>
    </td>
   </tr>
  <% } } else { %>
   <tr><td colspan="9" class="loan-empty">No loan records found.</td></tr>
  <% } %>
  </tbody>
 </table>
 </div>
</main>
</body>
</html>