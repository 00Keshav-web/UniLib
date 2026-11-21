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
        response.sendRedirect("login.jsp"); return;
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
<link rel="stylesheet" href="css/style.css">
<style>
.loan-container{max-width:1400px;margin:35px auto;padding:24px}
.loan-heading{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-bottom:22px}
.loan-filters{display:flex;gap:10px;flex-wrap:wrap;margin:20px 0}
.loan-filters input,.loan-filters select{padding:12px;border:1px solid #d5dce5;border-radius:8px}
.loan-filters input{flex:1;min-width:220px}
.loan-filters button{padding:0 22px;background:#e0a91b;border:0;border-radius:8px;font-weight:700;cursor:pointer}
.loan-table-wrap{overflow-x:auto;background:white;border-radius:12px;box-shadow:0 3px 12px rgba(0,0,0,.06)}
.loan-table{width:100%;border-collapse:collapse;min-width:1150px}
.loan-table th,.loan-table td{padding:13px;text-align:left;border-bottom:1px solid #e5e7eb;vertical-align:middle}
.loan-table th{background:#24466f;color:white}
.loan-table form{margin:0}
.return-button{padding:8px 11px;background:#24466f;color:white;border:0;border-radius:6px;cursor:pointer}
.return-button:hover{background:#193655}
.loan-status{font-size:12px;font-weight:700}
.status-overdue{color:#b42318}.status-borrowed{color:#175cd3}.status-returned{color:#067647}
.loan-notice{padding:12px 16px;margin:12px 0;border-radius:8px;background:#f0fdf4;color:#166534}
.loan-notice.error{background:#fef2f2;color:#991b1b}
.loan-empty{text-align:center;padding:35px;color:#6b7280}
.loan-muted{color:#6b7280;font-size:13px}
</style>
</head>
<body>
<header class="dashboard-header">
 <div class="dashboard-logo">UniLib | Manage Loans</div>
 <div><a href="librarian-dashboard">Dashboard</a>&nbsp;&nbsp;<a href="logout">Logout</a></div>
</header>
<main class="loan-container">
 <div class="loan-heading">
  <div><h1>Loan Management</h1><p class="loan-muted">Review borrowing transactions and process book returns.</p></div>
  <a href="librarian-dashboard" style="color:#24466f;font-weight:600">← Back to Dashboard</a>
 </div>
 <% if ("returned".equals(success)) { %><div class="loan-notice">Book return recorded successfully and member notified.</div><% } %>
 <% if ("invalid".equals(error)) { %><div class="loan-notice error">Invalid transaction ID.</div>
 <% } else if ("notactive".equals(error)) { %><div class="loan-notice error">This loan is no longer active or could not be found.</div><% } %>
 <form class="loan-filters" action="loans" method="get">
  <input type="search" name="search" placeholder="Search member, membership ID, book or transaction ID" value="<%= h(search) %>">
  <select name="status">
   <option value="ALL" <%= "ALL".equals(selectedStatus) ? "selected" : "" %>>All statuses</option>
   <option value="BORROWED" <%= "BORROWED".equals(selectedStatus) ? "selected" : "" %>>Active loans</option>
   <option value="OVERDUE" <%= "OVERDUE".equals(selectedStatus) ? "selected" : "" %>>Overdue</option>
   <option value="RETURNED" <%= "RETURNED".equals(selectedStatus) ? "selected" : "" %>>Returned</option>
  </select>
  <button type="submit">Filter</button>
  <a href="loans" style="align-self:center;color:#24466f;font-weight:600">Clear</a>
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
      <form action="return-loan" method="post" onsubmit="return confirm('Confirm that this book has been returned?');">
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