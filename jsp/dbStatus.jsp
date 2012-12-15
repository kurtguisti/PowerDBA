<%@ page import="com.powerdba.jdbc.*"%>
<%@ page import="com.powerdba.mvc.*"%>
<%@ page import="com.powerdba.mvc.jsp.*"%>
<%@ page import="com.powerdba.ProcessDAO"%>
<%@ page import="com.powerdba.util.StringUtility"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Date"%>
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
      //  pageContext = (PageContext) env.getPageContext();
      //  pageContext.forward("/adminLogin.jsp?formaction="+PowerDbaActions.ADMIN_LOGIN+"&first=Y&adminusername=admin");
      //}
      //
       
      int formAction = env.getInt("formaction");
      if ( formAction == PowerDbaActions.REINIT_POOLS ) {
      //  ConnectionManager.init();
        ConnectionConfigurationHolder.forceReload();
      }
%>

<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <title>Power*DBA Administrator - DB Connection Status</title>
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
     function reinit(myForm) { 
       myForm.formaction.value = "<%= PowerDbaActions.REINIT_POOLS %>";                 
       myForm.action = "dbStatus.jsp"; 
       myForm.submit(); 
     }
   </SCRIPT>
   <META HTTP-EQUIV="Refresh" CONTENT="3;URL=dbStatus.jsp">
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<!-- Body Definition -->
<body text="#000000" bgcolor="#FFFFFF" link="#FFFFFF"
      vlink="#FFFFFF" alink="#FFFFFF" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0">

<form method="post" action="adminLogin.jsp?adminusername=admin" name="connections" onSubmit="valLogin();return false;">
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
                <!--
                <td>|</td>
                <td><%= HtmlComponent.getExtraSmallButton("Descriptor Maintenance","adminLogin.jsp?adminusername=admin","")%><td>
                -->
</tr>

</table></td></tr></table></center></td></tr>
<tr>
<td bgcolor=black><img src='images/clear.gif' height=1></td>
</tr>
</table>

<br>

<table cellspacing="0" cellpadding="0" border="0" width="100%">  
   <tr><td colspan=2>&nbsp;&nbsp;<b>Database Instance Status (refreshes every 3 seconds)</b></td>
<td>
<table>
  <tr>
    <td><%=HtmlComponent.getSmallButton("Re-Init","","reinit(connections);return false;",85,15)%></td>
  </tr>
</table>
</td></tr>
</table>
   
<br>

  <center>
  <table border=1 width=90% cellpadding=1 cellspacing=0>
  <tr>
    <th bgcolor=#eeeeee><font size=-1>Pool</font></th>
    <th bgcolor=#eeeeee><font size=-1>Status</font></th>
    <th width=130 bgcolor=#eeeeee><font size=-1>Last Status</font></th>
    <th bgcolor=#eeeeee><font size=-1>State Cnt</font></th>
    <th bgcolor=#eeeeee><font size=-1>Time to Acquire State (ms)</font></th>
  </tr>
  <% ArrayList pools = ConnectionConfigurationHolder.getPoolList();
     Hashtable configs = ConnectionConfigurationHolder.getUrlHash();
     for ( int i=0; i<pools.size(); i++ ) {
       DbConfig dbConfig = (DbConfig) pools.get(i); 
       DbConfig dbConfig2 = (DbConfig) configs.get(dbConfig.getConnectString());
       if ( dbConfig2 == null ) dbConfig2 = new DbConfig();
       DbState lastState = dbConfig.getLastState(); 
       
       
       String bgcolor = null;
       if ( lastState.getStatus() == DbState.DOWN ) {
         bgcolor = "red";
       } else if ( lastState.getStatus() == DbState.UP ) {
         bgcolor = "chartreuse";
       } else {
         bgcolor = "yellow";
       }
       
       String statusString = null;
       if (lastState.getStatus() == DbState.CHECKING_IN_PROGRESS ) {
    	   String ssd = null;
           if (        lastState.getSubstatus() == DbState.CHECKING_GETCONN ) {
               ssd = "Getting Connection";
           } else if ( lastState.getSubstatus() == DbState.CHECKING_PARSE ) {
               ssd = "Parsing Query";
           } else if ( lastState.getSubstatus() == DbState.CHECKING_EXEC ) {
               ssd = "Executing Query";
           } else if ( lastState.getSubstatus() == DbState.CHECKING_FETCH ) {
               ssd = "Fetching Result";
           } else {
               ssd = "Unable to determine Substatus"; 
           }
           statusString  = lastState.getStatusMessage() + " " + ssd + " " + " (" + ((System.currentTimeMillis()-lastState.getStartTime())/1000) + "s) ";
       } else { 
           statusString = lastState.getStatusMessage();
       } %>
       <tr>

       <td align=center bgcolor=<%= bgcolor %>> <%= HtmlComponent.getExtraSmallButton(StringUtility.initCap(dbConfig.getPoolName()), "powerdba.jsp?formaction=1127&database=" + dbConfig.getPoolName(),"") %>
       <td align=center width=300 bgcolor=<%= bgcolor %>><font size=-2><b><%= statusString %></b></font></td>
       <td align=center           bgcolor=<%= bgcolor %>><font size=-2><b><%= new java.util.Date(lastState.getStatusDate()).toString()%></b></font></td>
       <td align=center           bgcolor=<%= bgcolor %>><font size=-1><b><%= dbConfig.getStates().size() %></b></font></td>
       <td align=center           bgcolor=<%= bgcolor %>><font size=-1><b><%= lastState.getSecondsToGet() %></b></font></td>
       <tr>
  <% } %>
  </table>
  


</form>

</body>
</html>

<%
}  catch (Exception e) {
  JspNavigation.toWsnError(new WsnException("dbStatus.jsp",e.getMessage()), env);
}
%>

