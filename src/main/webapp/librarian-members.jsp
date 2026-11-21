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
        response.sendRedirect("login.jsp"); return;
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
<link rel="stylesheet" href="css/style.css">
<style>
.member-container{max-width:1200px;margin:35px auto;padding:24px}
.member-heading{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-bottom:22px}
.member-table-wrap{overflow-x:auto;background:#fff;border-radius:12px;box-shadow:0 3px 12px rgba(0,0,0,.06)}
.member-table{width:100%;border-collapse:collapse;min-width:850px}
.member-table th,.member-table td{padding:14px;text-align:left;border-bottom:1px solid #e5e7eb}
.member-table th{background:#24466f;color:white}
.member-search{display:flex;gap:10px;margin:20px 0;flex-wrap:wrap}
.member-search input{flex:1;min-width:220px;padding:12px;border:1px solid #d5dce5;border-radius:8px}
.member-search button{padding:0 22px;background:#e0a91b;border:0;border-radius:8px;font-weight:700;cursor:pointer}
.member-link{color:#24466f;font-weight:700}
.member-muted{color:#6b7280;font-size:13px}
.member-empty{text-align:center;padding:35px;color:#6b7280}
.member-count{margin:14px 0;color:#6b7280}
</style>
</head>
<body>
<header class="dashboard-header">
 <div class="dashboard-logo">UniLib | Manage Members</div>
 <div><a href="librarian-dashboard">Dashboard</a>&nbsp;&nbsp;<a href="logout">Logout</a></div>
</header>
<main class="member-container">
 <div class="member-heading">
  <div><h1>Member Directory</h1><p class="member-muted">View registered members and their borrowing activity.</p></div>
  <a class="member-link" href="librarian-dashboard">← Back to Dashboard</a>
 </div>
 <% if ("invalid".equals(error) || "notfound".equals(error)) { %>
 <p class="member-muted">The requested member could not be found.</p>
 <% } %>
 <form class="member-search" action="members" method="get">
  <input type="search" name="search" placeholder="Search name, email or membership ID" value="<%= h(search) %>">
  <button type="submit">Search</button>
  <a class="member-link" href="members" style="align-self:center">Clear</a>
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
    <td><a class="member-link" href="member?id=<%= member.getId() %>">View details</a></td>
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