<%@ page import="com.powerdba.jdbc.*"%>
<%@ page import="com.powerdba.mvc.*"%>
<%@ page import="com.powerdba.util.*"%>
<%@ page import="com.powerdba.mvc.jsp.*"%>
<%@ page import="com.powerdba.ProcessDAO"%>
<%@ page import="com.powerdba.jdbc.DbConfig"%>

<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
  JspEnvironment env = null;
  int formAction = 0;
  try {

    boolean success = false;
    
    env = new JspEnvironment (request, response, pageContext);
    
    Boolean adminConnected = (Boolean) env.getSessionAttribute("adminconnected");
    if ( adminConnected == null ) adminConnected = new Boolean(false); 
    if ( !adminConnected.booleanValue() ) {
      // forward to login.jsp
      pageContext = (PageContext) env.getPageContext();
      pageContext.forward("/adminLogin.jsp?formaction="+PowerDbaActions.ADMIN_LOGIN+"&first=Y&username=admin");
    }
    
    Hashtable connectionHash = new DbConfigDAO().getHash("dbconfig.xml", false);
    String poolName = env.getParameter("poolname");
    DbConfig dc = (DbConfig) connectionHash.get(poolName);
    if ( dc == null ) dc = new DbConfig();
    formAction = env.getInt("formaction");
    
%>

<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <title>Power*DBA Administrator - Configuration</title>
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
   function save(myForm) { 
     myForm.formaction.value = "<%= PowerDbaActions.SAVE %>";                 
     myForm.action = "connectionDetail.jsp"; 
     myForm.submit(); 
   }
   function clone(myForm) { 
     myForm.formaction.value = "<%= PowerDbaActions.CLONE %>";                 
     myForm.action = "connectionDetail.jsp"; 
     myForm.submit(); 
   }
   function deleteconfig(myForm) { 
     myForm.formaction.value = "<%= PowerDbaActions.DELETE %>";                 
     myForm.action = "connectionDetail.jsp"; 
     myForm.submit(); 
   }
   function test_connect(myForm) { 
     myForm.formaction.value = "<%= PowerDbaActions.TEST_CONNECTION %>";                 
     myForm.action = "connectionDetail.jsp"; 
     myForm.submit(); 
   }
   </SCRIPT>
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<!-- Body Definition -->
<body text="#000000" bgcolor="#FFFFFF" link="#000000" vlink="#000000" alink="#000000" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0" onLoad="document.conndetail.poolname.focus();">

<form method="post" action="connectionDetail.jsp" name="conndetail">
<input type="hidden" name="formaction" value=<%=env.getInt("formaction")%>

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


</tr>

</table></td></tr></table></center></td></tr>
<tr>
<td bgcolor=black><img src='images/clear.gif' height=1></td>
</tr>
</table>

<br>

<table cellspacing="0" cellpadding="0" border="0" width="100%">  
   <tr><td colspan=2><b>&nbsp;
     <a class=link href="maintConnections.jsp">Database Connection Descriptors</a> &raquo; <%=poolName%></b>
   </td></tr>
</table>

<table><tr>
<%     
      switch (formAction) {
      
         case PowerDbaActions.SAVE:
         
           Tracer.log("Processing SAVE", Tracer.DEBUG, this);                

           dc = new DbConfig();
           dc.setPoolName(poolName);
           dc.setDbDriver(env.getParameter("driver"));
           dc.setDatabaseName(env.getParameter("databasename"));
           dc.setConnectString(env.getParameter("connectstring"));
           dc.setLogin(env.getParameter("pdbausername"));
           dc.setPassword(env.getParameter("pdbapassword"));
           dc.setMonitor( (env.getParameter("pdbamonitor").equals("Y")?true:false) );
           
           connectionHash.put(poolName, dc);
           
           // Save only the changed pool definititon back to the xml file...
           String msg = new DbConfigDAO().saveDbConfig(dc, "dbconfig.xml"); 
           %>
           <td>&nbsp;<%=msg%></td>
           <%
           break;
           
         case PowerDbaActions.CLONE:
           Tracer.log("Processing CLONE", Tracer.DEBUG, this);                

           dc = new DbConfig();
           dc.setPoolName("<NEW NAME>");
           dc.setDbDriver(env.getParameter("driver"));
           dc.setDatabaseName(env.getParameter("databasename"));
           dc.setConnectString(env.getParameter("connectstring"));
           dc.setLogin(env.getParameter("username"));
           dc.setPassword(env.getParameter("password"));
           %>
           <td>&nbsp;Enter new information</td>
           <%
           break;
           
         case PowerDbaActions.DELETE:
           Tracer.log("Processing DELETE", Tracer.DEBUG, this);       
           // Delete the pool save the in-memory hash version to disk and then retrieve it back into the Vector
           connectionHash = new DbConfigDAO().getHash("dbconfig.xml");
           connectionHash.remove(env.getParameter("poolname"));
           new DbConfigDAO().saveHash(connectionHash, "dbconfig.xml");
           dc = new DbConfig();
           %>
           <td>&nbsp;Descriptor <%= poolName %> Deleted...</td>
           <%
           break;
           
         case PowerDbaActions.TEST_CONNECTION:
           Tracer.log("Processing TEST", Tracer.DEBUG, this);                

           dc = new DbConfig();
           dc.setPoolName(poolName);
           dc.setDbDriver(env.getParameter("driver"));
           dc.setDatabaseName(env.getParameter("databasename"));
           dc.setConnectString(env.getParameter("connectstring"));
           dc.setLogin(env.getParameter("username"));
           dc.setPassword(env.getParameter("password"));

           try {
             ProcessDAO.testConnection(dc); 
             %>
               <td>&nbsp;&nbsp;&nbsp;<font color='green'>Successfully Connected...</font></td>
             <%
           } catch ( SQLException e) {
           %>
             <td>&nbsp;<font color='red'>Error connecting to <%=dc.getConnectString()%>...<br>
             <b>Cause:</b>&nbsp;<%=e.getMessage()%>
             </font></td>
           <%}
           break;
           
        }        
%>
</tr></table>
   
<br>

<table border=1 cellspacing=1 cellpadding=4>
  <tr>
    <th><font size=-1>Pool Name</font></th><td><%=HtmlComponent.getInput("poolname",180,180,"input",dc.getPoolName(),false,null,"smallentry")%></td>
  </tr>
  <tr>
    <th><font size=-1>Database Name</font></th><td><%=HtmlComponent.getInput("databasename",180,180,"input",dc.getDatabaseName(),false,null,"smallentry")%></td>
  </tr>
  <tr>
    <th><font size=-1>Driver Class</font></th><td><%=HtmlComponent.getInput("driver",180,180,"input",dc.getDbDriver(),false,null,"smallentry")%></td>
  </tr>
  <tr>
    <th><font size=-1>JDBC URL</font></th><td><%=HtmlComponent.getInput("connectstring",180,180,"input",dc.getConnectString(),false,null,"smallentry")%></td>
  </tr>
  <tr>
    <th><font size=-1>DBA Username</font></th><td><%=HtmlComponent.getInput("pdbausername",180,180,"input",dc.getLogin(),false,null,"smallentry")%></td>
  </tr>
  <tr>
    <th><font size=-1>DBA Password</font></th><td><%=HtmlComponent.getInput("pdbapassword",180,180,"password",dc.getPassword(),false,null,"smallentry")%></td>
  </tr>
  <tr>
    <th><font size=-1>Monitor?</font></th><td><%=HtmlComponent.getInput("pdbamonitor",180,180,"input",dc.isMonitor()?"Y":"N",false,null,"smallentry")%></td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><%=HtmlComponent.getSmallButton("Save","","save(conndetail); return false;",85,15)%></td>
    <td>&nbsp;</td>
    <td><%=HtmlComponent.getSmallButton("New","connectionDetail.jsp","",85,15)%></td>
    <td>&nbsp;</td>
    <td><%=HtmlComponent.getSmallButton("Delete","","deleteconfig(conndetail); return false;",85,15)%></td>
    <td>&nbsp;</td>
    <td><%=HtmlComponent.getSmallButton("Test","","test_connect(conndetail); return false;",85,15)%></td>
    <td>&nbsp;</td>
    <td><%=HtmlComponent.getSmallButton("Connections","maintConnections.jsp","",85,15)%><td>
  </tr>
</table>

</form>

</body>
</html>

<%
}  catch (Exception e) {
  JspNavigation.toWsnError(new WsnException("Power*DBA Error executing action " + formAction,e.getMessage()), env);
}
%>


