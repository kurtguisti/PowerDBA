<%@ page import="com.powerdba.mvc.jsp.*,
                 com.powerdba.mvc.PerfStatManager,
                 com.powerdba.mvc.PerfStatView,
                 com.powerdba.PerfStatDAO,
                 com.powerdba.MenuGenerator,
                 com.powerdba.PerfStatDAO,
                 java.sql.Timestamp,
                 com.powerdba.mvc.*,
                 com.powerdba.util.*"%>
<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
  JspEnvironment env = null;
  try {
    env = new JspEnvironment (request, response, pageContext);
    PerfStatManager myMgr  = new PerfStatManager(env);
    PerfStatView    myView = (PerfStatView) myMgr.process();
    String thisJsp = "perfstat.jsp";
    String thisForm = "perfstat";
    String version = "1.0.7b";
%>

<html>
<head>
   <title>Power*DBA Historical Analysis</title>
   <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
   <link REL="StyleSheet" TYPE="text/css" HREF="css/powerdba.css">
   <SCRIPT>
      function go(myForm, dbaction, pagetype) {               
        myForm.formaction.value = dbaction; 
        myForm.pagetype.value = pagetype;
        myForm.submit();                                   
      } 
      
    </SCRIPT>
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<body text="#000000" bgcolor="#ffffff" link="#003399" vlink="#551A8B" alink="#FF0000" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0" onLoad="<%= myView.getOnLoadActions()%>">

<form method="post" action="perfstat.jsp" name="perfstat">
<input type="hidden" name="formaction" value="<%=myView.getCurrAction()%>">
<input type="hidden" name="action"     value="0">
<input type="hidden" name="pagetype"   value="">

<%@ include  file="chartHeaderInclude.jsp"%>

<left>
<table>

<% if ( myView.getMessage().trim().length() > 0 ) { %>
  <tr>
    <td colspan=10><font size='-1' color='red'><b><%= StringUtility.replace(myView.getMessage()," ","&nbsp;") %></b></font></td>
  </tr>
<% } %>
                          
<% if ( myView.isConnected() ) { %>
  <tr>
    <td colspan=5><font size='-1'><b>
      &nbsp;&nbsp;<%=myView.getCurrDatabase() + "&nbsp;->&nbsp;" + "Chart Zoom"%></b></font>
    </td>
  </tr>
<% } %>
  
<% if ( myView.isConnected() ) { %>
  <% if ( myView.getHtml() != null ) { %> 
    <%=myView.getHtml()%>
  <% } %> 
<% } %>
  
</table>

</form>
</center>
</body>
</html>

<%
    }  catch (WsnException wsn) {
       JspNavigation.toWsnError(wsn, env);
    }
%>