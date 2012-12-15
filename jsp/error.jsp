<%@ page import="com.powerdba.mvc.WsnException,
                 com.powerdba.mvc.jsp.JspEnvironment,
                 java.io.PrintWriter;"%>
<html>
<head>
   <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
   <TITLE>Power*DBA Error page</TITLE>
   <link REL="StyleSheet" TYPE="text/css" HREF="../css/pmnew.css">
   <STYLE type="text/css">
        A:link	  { color:#ffffff;}
        A:visited	{ color:#ffffff;}
        A:hover		{ color:#FF3300;}
   </STYLE>
</head>
<body text="#000000" bgcolor="#FFFFFF" link="#003399" vlink="#551A8B" alink="#FF0000" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">
<%
    JspEnvironment env = new JspEnvironment (request, response, pageContext);
    WsnException wsn = (WsnException)env.getSessionAttribute("wsnException");
    if ( wsn == null ) {
    	wsn = new WsnException("Unknown Error","");
    }
%>

<table width=100% border=0 cellpadding=0 cellspacing=1><tr><td bgcolor='#cccccc'>
<center><table width=100%><tr><td bgcolor='#cccccc'><left><table>
<tr>
<td><img src='images/power1.jpg' border=0></td>
<td><img src='images/clear.gif' width=10></td>
<td valign='bottom'><font color='red'><b>An Error has Occurred in the Power*DBA Server</b></font></td>
</tr>
</table></td></tr></table></center></td></tr></table>

<table width=90%>

<br><br>

<tr><th align='left'>Error Message:&nbsp</th></tr>
<tr><td><%= wsn.getDetails() %></td></font></tr>

<tr><td></td></tr>

<tr><th align='left'>Error Context:&nbsp;</th></tr>
<td><%=wsn.getTitle()%></td>

<tr><td></td></tr>
<tr><td></td></tr>

<tr><th align='left'>Stack Trace:&nbsp;</th></tr>
<tr><td> <% wsn.getT().printStackTrace(new PrintWriter(out)); %> </td></tr>

</table>
</center>


<%env.removeSessionAttribute("wsnException");%>

</center>
</body>
</html>

