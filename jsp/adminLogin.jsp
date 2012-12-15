<%@ page import="com.powerdba.mvc.*,
                 com.powerdba.mvc.LoginManager,
                 com.powerdba.mvc.LoginView,
                 com.powerdba.mvc.jsp.*,
                 com.powerdba.util.Tracer"%>
<%@ page import="com.powerdba.util.*"%>
<%@ page import="java.util.*"%>
<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
  Tracer.log("In adminLogin.jsp",Tracer.DEBUG, "");
  JspEnvironment env = new JspEnvironment (request, response, pageContext);

try {

  LoginManager loginMgr = new LoginManager(env);
  LoginView    myView = (LoginView) loginMgr.process();
%>

<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <title>Power*DBA Administrator - Login Screen</title>
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
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<%= myView.getJavaScript() %>

<!-- Body Definition -->
<body text="#000000" bgcolor="#FFFFFF" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0" onLoad="document.login.adminpassword.focus();">

<form method="post" action="adminLogin.jsp" name="login">
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


</tr>

</table></td></tr></table></center></td></tr>
<tr>
<td bgcolor=black><img src='images/clear.gif' height=1></td>
</tr>
</table>

<table cellspacing="0" cellpadding="0" border="0" width="100%">
   
  <tr>
    <td align="left" width="15%"><img src="images/clear.gif" height=20></td>
  </tr>

  <tr>
    <td colspan=2>&nbsp;&nbsp;<font size=+0 color=#333333><b>Power*DBA Admininistrator Login</b></font></td>
  </tr>
   
  <tr>
    <td align="left" width="15%"><img src="images/clear.gif" height=20></td>
  </tr>

  <tr>
    <td align="left" width="15%"><img src="images/clear.gif"></td>
    <td colspan=2><b><%= myView.getMessage()%></b></td>
  </tr>

  <tr valign="bottom">
    <td align="left" width="15%"><img src="images/clear.gif"></td>
    <td align="left" width="25%"><div class="form"><font size=-1>Username</font><br>
    <input type='text' name='adminusername' size=10 maxlength=20 value='<%=env.getParameter("adminusername")%>' class='smallentry'> <br></div></td>
  </tr>
   
  <tr valign="bottom">
	<td align="left"><img src="images/clear.gif"></td>
	<td align="left" class="smallentry"><font size=-1>Password</font><br>
	<input type='password' name='adminpassword' size=10 maxlength=20 value='<%=env.getParameter("adminpassword")%>' class='smallentry'>
	<br></td>
  </tr>
      
  <tr><td>&nbsp;</td></tr>
      
  <tr>
    <td align="left"><div class="smallentry"><img src="images/clear.gif"></div></td>
    <td align="left"><%=HtmlComponent.getSmallButton("Login", "", "valLogin(); return false;", 90, 15)%></td>
  </tr>
   
</table>

</form>

</body>
</html>

<%
}  catch (WsnException wsn) {
  JspNavigation.toWsnError(wsn, env);
}
%>
