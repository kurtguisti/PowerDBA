<%@ page import="com.powerdba.mvc.jsp.*,com.powerdba.mvc.*,com.powerdba.util.*"%>

<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
    JspEnvironment env = null;
    try {
       env = new JspEnvironment(request, response, pageContext);
%>

<html>
<head>
   <title>Power*DBA SQL Popup</title>
   <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
   <link REL="StyleSheet" TYPE="text/css" HREF="../css/pmnew.css">
</head>

<body text="#000000" bgcolor="#cccccc" link="#003399" vlink="#551A8B" alink="#FF0000" topmargin="0"
leftmargin="0" marginheight="0" marginwidth="0">

<%= com.powerdba.mvc.PopupView.getQueryPopupHtml(env.getParameter("database"), env.getParameter("query")) %>


</center>
</body>
</html>

<%
    }  catch (WsnException wsn) {
       JspNavigation.toWsnError(wsn, env);
    } catch (Exception wsn) {
       JspNavigation.toWsnError(new WsnException("Error","Error"), env);
    }
%>