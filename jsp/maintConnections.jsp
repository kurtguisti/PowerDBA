<%@ page import="com.powerdba.jdbc.*"%>
<%@ page import="com.powerdba.mvc.*"%>
<%@ page import="com.powerdba.mvc.jsp.*"%>
<%@ page import="com.powerdba.ProcessDAO"%>
<%@ page import="com.powerdba.util.PropertyHolder"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
    JspEnvironment env = null;
    boolean doTest = false;
    try {
    
      env = new JspEnvironment (request, response, pageContext);
       
      // Check for security
      //Boolean adminConnected = (Boolean) env.getSessionAttribute("adminconnected");
      //if ( adminConnected == null ) adminConnected = new Boolean(false); 
      //if ( !adminConnected.booleanValue() ) {
        // forward to login.jsp
        pageContext = (PageContext) env.getPageContext();
        pageContext.forward("/adminLogin.jsp?formaction="+PowerDbaActions.ADMIN_LOGIN+"&first=Y&username=admin");
      //}
      //
       
       Vector connectionList = new DbConfigDAO().getOrderedConfigList("dbconfig.xml");
       
       int formAction = env.getInt("formaction");
       if ( formAction == PowerDbaActions.TEST_CONNECTION ) doTest = true;
       //if ( formAction == PowerDbaActions.REINIT_POOLS ) {
       //  ConnectionManager.init();
       //  ConnectionConfigurationHolder.forceReload();
       //}
%>

<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <title>Power*DBA Administrator - DB Connections</title>
   <link REL="StyleSheet" TYPE="text/css" HREF="../css/pmnew.css">
   <STYLE TYPE="text/css">
     .smallentry { font-size: 8pt; color: #222222 }
     .verysmallentry { font-size: 7pt; color: #222222 }
     .linkSmallWhite {font: bold 9px verdana; color: #dddddd; padding: 1px 1px 1px 1px; text-decoration: none; }
     .linkSmall {font: 9px verdana; color: #003399; padding: 1px 1px 1px 1px; text-decoration: underline; }
     .smallSelect {font: 9px verdana; color: #333333; padding: 0px 0px 0px 0px; text-decoration: none; }
     .smallSelectBold {font: bold 9px verdana; color: #333333; padding: 0px 0px 0px 0px; text-decoration: none; }
     .textBlock {padding: 3px;border: 1px solid #dddddd;background-color: #cccc99;width:1000px;}
     .searchField {font:  9px #222222; margin: 0px; width: 140;border: 1px solid #73969c; position: relative;left: -2px;}
   </STYLE>
   <SCRIPT>
     function test_connect(myForm) { 
       myForm.formaction.value = "<%= PowerDbaActions.TEST_CONNECTION %>";                 
       myForm.action = "maintConnections.jsp"; 
       myForm.submit(); 
     }
     function reinit(myForm) { 
       myForm.formaction.value = "<%= PowerDbaActions.REINIT_POOLS %>";                 
       myForm.action = "maintConnections.jsp"; 
       myForm.submit(); 
     }
   </SCRIPT>
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<!-- Body Definition -->
<body text="#000000" bgcolor="#FFFFFF" link="#FFFFFF"
      vlink="#FFFFFF" alink="#FFFFFF" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0">

<form method="post" action="maintConnections.jsp" name="connections" onSubmit="valLogin();return false;">
<input type="hidden" name="formaction" value=<%=env.getInt("formaction")%>>
<input type="hidden" name="target" value=0>
<input type="hidden" name="source" value=0>
<input type="hidden" name="mode" value=0>

<!-- Page Specific Stuff -->

<table width=100% border=0 cellpadding=0 cellspacing=0>
  <tr>
    <td bgcolor='#cccccc'>
      <center>
      <table width=100%>
        <tr>
          <td bgcolor='#cccccc'>
            <left>
            <table>
              <tr>
                <td><img src='images/power1.jpg' border=0></td>
                <td><img src='images/clear.gif' width=5 height=1></td>
                <td>&nbsp;&nbsp;</td>
                <td><%= HtmlComponent.getExtraSmallButton("Return to Power*DBA", "powerdba.jsp","") %></td>
                <td>|</td>
                <td><%= HtmlComponent.getExtraSmallButton("Status Page", "dbStatus.jsp","") %></td>


</tr>

</table></td></tr></table></center></td></tr>
<tr>
<td bgcolor=black><img src='images/clear.gif' height=1></td>
</tr>
</table>

<br>

<table cellspacing="0" cellpadding="0" border="0" width="100%">  
   <tr><td colspan=2>&nbsp;&nbsp;<b>Database Connection Descriptors</b></td></tr>
</table>
   
<br>

<table border=1 cellspacing=0 cellpadding=2>
  <tr>
    <th bgcolor=#eeeeee><font size=-1>Pool Name</font></th>
    <th bgcolor=#eeeeee><font size=-1>Database Name</font></th>
    <th bgcolor=#eeeeee><font size=-1>Driver Class</font></th>
    <th bgcolor=#eeeeee><font size=-1>JDBC URL</font></th>
    <th bgcolor=#eeeeee><font size=-1>User</font></th>
    <th bgcolor=#eeeeee><font size=-1>Monitor?</font></th>
    <th bgcolor=#eeeeee><font size=-1>Connect Status</font></th>
    <th bgcolor=#eeeeee><font size=-1>&nbsp;</font></th>
  </tr>

  <% for (int i=0; i<connectionList.size(); i++ ) {
       DbConfig dc = (DbConfig) connectionList.get(i); %>
    <tr>
      <td bgcolor=#eeeeee align=center><font size=-1><%= dc.getPoolName() %></font></td>
      <td bgcolor=#eeeeee align=center><font size=-1><%= dc.getDatabaseName() %></font></td>
      <td bgcolor=#eeeeee align=center><font size=-1><%= dc.getDbDriver() %></font></td>
      <td bgcolor=#eeeeee align=center><font size=-1><%= dc.getConnectString() %></font></td>
      <td bgcolor=#eeeeee align=center><font size=-1><%= dc.getLogin() %></font></td>
      <td bgcolor=#eeeeee align=center><font size=-1><%= (dc.isMonitor()==true?"Y":"N") %></font></td>
     
      <% if ( doTest ) { 
        try {
          ProcessDAO.testConnection(dc); %>
          <td bgcolor=#eeeeee align=center><font size=-1 color="Green">Success...</font></td>
        <% } catch (SQLException e) { %>
              <td bgcolor=#eeeeee align=center><font size=-1 color="Red"><%=e.getMessage()%></font></td>
        <% } %>
      <% } else { %>
             <td bgcolor=#eeeeee align=center><font size=-1>Not Tested</font></td>
      <% } %>
      
      <td bgcolor=#eeeeee align=center><b><%=HtmlComponent.getExtraSmallButton("Properties","connectionDetail.jsp?poolname=" + dc.getPoolName(),"")%></td>
      
    </tr>
  <% } %>

</table>

<br>
<table>
  <tr>
    <td><%=HtmlComponent.getSmallButton("Create New","connectionDetail.jsp","",85,15)%></td>
    <td>&nbsp;</td>
    <td><%=HtmlComponent.getSmallButton("Test All","","test_connect(connections);return false;",85,15)%></td>
    <td>&nbsp;</td>
    <td><%=HtmlComponent.getSmallButton("Re-Init","","reinit(connections);return false;",85,15)%></td>
  </tr>
</table>

<br>
<pre>
<%= PropertyHolder.dumpProperties() %>
</pre>

</form>

</body>
</html>

<%
}  catch (WsnException wsn) {
  JspNavigation.toWsnError(wsn, env);
}
%>

