<%@ page import="com.powerdba.mvc.jsp.*,
                 com.powerdba.mvc.PowerDbaView,
                 com.powerdba.mvc.PowerDbaManager"%>
<%@ page import="com.powerdba.mvc.*"%>
<%@ page import="com.powerdba.util.*"%>
<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
    JspEnvironment env = null;

    try {
       env = new JspEnvironment (request, response, pageContext);
       ProcessManager myMgr = new ProcessManager(env);
       ProcessView myView = (ProcessView) myMgr.process();
%>

<html>
<head>
  <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
  <TITLE>Power*DBA Explain</TITLE>
  
  <link REL="StyleSheet" TYPE="text/css" HREF="../css/pmnew.css">
  <%= HtmlComponent.getCalendar1JsRef() %>
  <%= HtmlComponent.getDynamicListJsRef() %>
  <%= myView.getJavaScript() %>
    
  <STYLE type="text/css">
    A:link	    { color:#ffffff;}
    A:visited	{ color:#ffffff;}
    A:hover		{ color:#FF3300;}
  </STYLE>
</head>

<body text="#000000" bgcolor="#FFFFFF" link="#003399" vlink="#551A8B" alink="#FF0000" topmargin="5"
leftmargin="10" marginheight="0" marginwidth="0" onLoad="<%= myView.getOnLoadActions()%>">

<form method="post" action="zoom.jsp" name="explainform">
<input type="hidden" name="formaction" value="<%=myView.getCurrAction()%>">
<input type="hidden" name="target"     value="0">
<input type="hidden" name="action"     value="0">
<input type="hidden" name="database"   value="<%=myView.getCurrDatabase()%>"
<input type="hidden" name="hash"       value="<%=myView.getHash()%>"

<left><table><tr><td><font size=+1 color='#003399'><b>&nbsp;<%=myView.getTitle()%></b></font></td></tr></table></left>

<% if ( myView.getMessage().trim().length() > 0 ) { %>
  <tr><td><font size='-1' color='red'><%= StringUtility.replace(myView.getMessage()," ","&nbsp;") %></font></td></tr>
<% } %>

<hr width=100%>

<table><tr><td>
<%=HtmlComponent.getExtraSmallButton("Re-Explain","zoom.jsp?formaction=" + WsnIndex.EXPLAIN_PLAN + "&hash="+myView.getHash()+
                                               "&database="+myView.getCurrDatabase(),null) %>
</td></tr></table>


<% if ( myView.getHtml() != null ) { %>  
  <%=myView.getHtml()%>
<% } %>

</form>

</center>
</body>
</html>

<%
    }  catch (WsnException wsn) {
       JspNavigation.toWsnError(wsn, env);
    }
%>
