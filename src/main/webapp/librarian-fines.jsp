<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.LibrarianFinesServlet.Fine" %>
<%@ page import="com.unilib.servlet.LibrarianFinesServlet.AccruingFine" %>
<%!
    private String h(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
%>
<%
    if (session.getAttribute("userId") == null) { response.sendRedirect("login.jsp"); return; }
    if (!"LIBRARIAN".equals(session.getAttribute("role"))) { response.sendError(403, "Librarian access required."); return; }
    List<Fine> fines = (List<Fine>) request.getAttribute("fines");
    List<AccruingFine> accruing = (List<AccruingFine>) request.getAttribute("accruingFines");
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Fine Management | UniLib</title>
<link rel="stylesheet" href="css/style.css">

</head>
<body>
<header class="dashboard-header">
 <div class="dashboard-logo">UniLib | Fine Management</div>
 <div><a href="librarian-dashboard">Dashboard</a>&nbsp;&nbsp;<a href="logout">Logout</a></div>
</header>
<main class="fine-container">
 <h1>Fine Management</h1>
 <p class="fine-muted">Late fee: ₹5 per day after the due date. No grace period or maximum cap.</p>
 <a href="librarian-dashboard" style="color:#24466f;font-weight:600">← Back to Dashboard</a>
 <% if ("paid".equals(success)) { %><div class="fine-notice">Payment recorded successfully.</div><% } %>
 <% if ("invalid".equals(error)) { %><div class="fine-notice error">Invalid fine record.</div>
 <% } else if ("already".equals(error)) { %><div class="fine-notice error">This fine is already paid or could not be found.</div><% } %>

 <section class="fine-section">
  <h2>Currently Accruing Fines</h2>
  <p class="fine-muted">These amounts update daily while the book remains overdue. A permanent fine record is created when the book is returned.</p>
  <div class="fine-table-wrap"><table class="fine-table">
   <thead><tr><th>Transaction</th><th>Member</th><th>Membership ID</th><th>Book</th><th>Due Date</th><th>Accrued Fine</th></tr></thead>
   <tbody>
   <% if (accruing != null && !accruing.isEmpty()) { for (AccruingFine fine : accruing) { %>
    <tr><td>#<%= fine.getTransactionId() %></td><td><%= h(fine.getMemberName()) %></td><td><%= h(fine.getMembershipId()) %></td><td><%= h(fine.getTitle()) %></td><td><%= h(fine.getDueDate()) %></td><td><strong>₹<%= fine.getAmount() %></strong></td></tr>
   <% } } else { %><tr><td colspan="6" class="fine-empty">No currently accruing fines.</td></tr><% } %>
   </tbody>
  </table></div>
 </section>

 <section class="fine-section">
  <h2>Finalized Fine Records</h2>
  <div class="fine-table-wrap"><table class="fine-table">
   <thead><tr><th>Fine</th><th>Transaction</th><th>Member</th><th>Membership ID</th><th>Book</th><th>Amount</th><th>Status</th><th>Created</th><th>Paid At</th><th>Recorded By</th><th>Action</th></tr></thead>
   <tbody>
   <% if (fines != null && !fines.isEmpty()) { for (Fine fine : fines) { %>
    <tr>
     <td>#<%= fine.getId() %></td><td>#<%= fine.getTransactionId() %></td>
     <td><%= h(fine.getMemberName()) %></td><td><%= h(fine.getMembershipId()) %></td>
     <td><%= h(fine.getTitle()) %></td><td><strong>₹<%= fine.getAmount() %></strong></td>
     <td class="fine-status <%= "PAID".equals(fine.getStatus()) ? "fine-paid" : "fine-unpaid" %>"><%= h(fine.getStatus()) %></td>
     <td><%= h(fine.getCreatedAt()) %></td><td><%= h(fine.getPaidAt()) %></td><td><%= h(fine.getRecordedBy()) %></td>
     <td>
      <% if ("UNPAID".equals(fine.getStatus())) { %>
       <form method="post" action="record-fine-payment" onsubmit="return confirm('Confirm that this fine payment has been received?');">
        <input type="hidden" name="fineId" value="<%= fine.getId() %>">
        <button class="fine-button" type="submit">Record Payment</button>
       </form>
      <% } else { %><span class="fine-muted">Completed</span><% } %>
     </td>
    </tr>
   <% } } else { %><tr><td colspan="11" class="fine-empty">No finalized fine records yet.</td></tr><% } %>
   </tbody>
  </table></div>
 </section>
</main>
</body>
</html>