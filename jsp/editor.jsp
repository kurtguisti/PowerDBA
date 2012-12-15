<%@ page import="com.powerdba.mvc.jsp.*,
                 com.powerdba.mvc.EditorManager,
                 com.powerdba.mvc.EditorView"%>
<%@ page import="com.powerdba.mvc.*"%>
<%@ page import="com.powerdba.util.*"%>

<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
    JspEnvironment env = null;
    try {
       env = new JspEnvironment (request., response, pageContext);
       EditorManager myMgr  = new EditorManager(env);
       EditorView    myView = (EditorView) myMgr.process();
%>

<html>
<head>
   <title>Power*DBA Editor</title>
   <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
   <link REL="StyleSheet" TYPE="text/css" HREF="../css/pmnew.css">
   <%= myView.getJavaScript() %>
</head>

<body text="#000000" bgcolor="#cccccc" link="#003399" vlink="#551A8B" alink="#FF0000" topmargin="0"
leftmargin="0" marginheight="0" marginwidth="0" onLoad="<%= myView.getOnLoadActions()%>">

<%= myView.getFormOpen() %>

<table width=100% border=0 cellpadding=0 cellspacing=0>
<tr>
<td bgcolor='#cccccc'>
<center>
<table width=100%><tr><td bgcolor='#cccccc'>
<left>
<table border=1>

<tr>
<td><img src='images/power1.jpg' border=0></td>
<td><img src='images/clear.gif' width=5 height=1></td>
</tr>
<tr>
<td>

<table border=1 cellpadding=0 cellspacing=0>
<tr><td colspan=5><%=myView.getEditHtml()%></td></tr>
<tr>
<td><table><tr><td><%=myView.getFormAction("submit")%></td>
               <td><%=myView.getFormAction("cancel")%></td>
           </tr>
    </table>
</td>
</tr>
</table>
</td></tr></table>

</form>

</center>
</body>
</html>

<%
    }  catch (WsnException wsn) {
       JspNavigation.toWsnError(wsn, env);
    }
%>