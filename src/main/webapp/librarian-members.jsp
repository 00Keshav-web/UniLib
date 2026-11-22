<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.unilib.servlet.LibrarianMembersServlet.Member" %>
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
    List<Member> members = (List<Member>) request.getAttribute("members");
    String search = (String) request.getAttribute("search");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Manage Members | UniLib</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

</head>
<body>
<header class="dashboard-header">
 <div class="dashboard-logo">UniLib | Manage Members</div>
 <div><a href="<%= request.getContextPath() %>/librarian-dashboard">Dashboard</a>&nbsp;&nbsp;<a href="<%= request.getContextPath() %>/logout">Logout</a></div>
</header>
<main class="member-container">
 <div class="member-heading">
  <div><h1>Member Directory</h1><p class="member-muted">View registered members and their borrowing activity.</p></div>
  <a class="member-link" href="<%= request.getContextPath() %>/librarian-dashboard">← Back to Dashboard</a>
 </div>
 <% if ("invalid".equals(error) || "notfound".equals(error)) { %>
 <p class="member-muted">The requested member could not be found.</p>
 <% } %>
 <form class="member-search" action="<%= request.getContextPath() %>/members" method="get">
  <input type="search" name="search" placeholder="Search name, email or membership ID" value="<%= h(search) %>">
  <button type="submit">Search</button>
  <a class="member-link" href="<%= request.getContextPath() %>/members" style="align-self:center">Clear</a>
 </form>
 <p class="member-count">Members found: <strong><%= members == null ? 0 : members.size() %></strong></p>
 <div class="member-table-wrap">
 <table class="member-table">
  <thead><tr><th>Name</th><th>Membership ID</th><th>Email</th><th>Joined</th><th>Total Loans</th><th>Active Loans</th><th>Profile</th></tr></thead>
  <tbody>
  <% if (members != null && !members.isEmpty()) { for (Member member : members) { %>
   <tr>
    <td><strong><%= h(member.getName()) %></strong></td>
    <td><%= h(member.getMembershipId()) %></td>
    <td><%= h(member.getEmail()) %></td>
    <td><%= h(member.getJoinedDate()) %></td>
    <td><%= member.getTotalLoans() %></td>
    <td><%= member.getActiveLoans() %></td>
    <td><a class="member-link" href="<%= request.getContextPath() %>/member?id=<%= member.getId() %>">View details</a></td>
   </tr>
  <% } } else { %>
   <tr><td colspan="7" class="member-empty">No members found.</td></tr>
  <% } %>
  </tbody>
 </table>
 </div>
</main>
</body>
</html>