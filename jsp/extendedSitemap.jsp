<%@ page import="com.powerdba.mvc.jsp.*,
                 com.powerdba.mvc.PowerDbaManager,
                 com.powerdba.mvc.PowerDbaView,
                 com.powerdba.MenuGenerator,
                 com.powerdba.mvc.*,
                 com.powerdba.util.*,
                 com.powerdba.Lookup,
                 com.powerdba.LookupDisplayGroup,
                 com.powerdba.OracleStatistic,
                 com.powerdba.mvc.WsnException,
                 java.sql.SQLException,
                 java.util.*,
                 com.powerdba.util.Tracer"%>
<%@ page buffer="32kb"%>
<%@ page errorPage="/error.jsp" %>

<%
  Tracer.log("***** In powerdba.jsp", Tracer.DEBUG, "powerdba.jsp");
  JspEnvironment env = null;
  
  try {
    
     env = new JspEnvironment (request, response, pageContext);
     PowerDbaManager myMgr  = new PowerDbaManager(env);
     PowerDbaView    myView = (PowerDbaView) myMgr.process();
     String thisJsp = "powerdba.jsp";
     Tracer.log("Here",Tracer.DEBUG,"");
%>

<html>
<head>
   <title>Power*DBA Extended Sitemap </title>
   <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
   <link REL="StyleSheet" TYPE="text/css" HREF="css/powerdba.css">
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<body text="#000000" bgcolor="#ffffff" link="#003399" vlink="#003399" alink="#003399" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0">
<img src='images/clear.gif' height=1 width=800>
<table width=90% border=0 cellpadding=0 cellspacing=0>
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
                <td><font size=+3><b>Extended Actions</b></font></td>

</tr>


</table>
</td>
</tr>
</table>
</center>
</td>
</tr>
</table>
                
<left>
<table cellspacing=1 cellpadding=1 border=1>

<% 
ArrayList<SelectEntry> list = myView.getActionSelectList();

ArrayList<SelectEntry> dbs = myView.getShortDbList();


Collections.sort(list, SelectEntry.CASE_INSENSITIVE_DISPLAY_ORDER);

%>
<tr>
	   <td>
	   <center><font size=-1>&nbsp;<b><i>Extended Action</i></b></font></center>
	   </td>
<%
for ( int j=0; j<dbs.size(); j++ ) {
	SelectEntry se = (SelectEntry) dbs.get(j);
	   %> 
	   <td>
	   <b><font size=-1>&nbsp;<%= se.getDisplay() %></font></b>
	   </td>
	<%
	} %>
	</tr>
	
<%
for ( int i=0; i<list.size(); i++ ) {
	SelectEntry seAction = list.get(i);
	%> <tr>
	   <td><b><font size=-1>&nbsp;<%= seAction.getDisplay() %></font></b>
	   </td>
	<%
	for ( int j=0; j<dbs.size(); j++ ) {
		SelectEntry se = (SelectEntry) dbs.get(j);
		%> 
		   <td><center><font size=-1>
		   <a href=powerdba.jsp?formaction=<%=seAction.getValue()%>&database=<%=se.getValue()%>&allinstances=N>
		   <img src='images/execute.jpeg' width=20 height=20 border=0></a>
		   </font></center></td>
		<%
	} %>
	   </tr> 
	<%
}
%>
</table>
</body>
</html>

<%
    } catch (WsnException wsn) {
       JspNavigation.toWsnError(wsn, env);
    } catch (Exception e) {
       JspNavigation.toWsnError(new WsnException("powerdba.jsp",e.getMessage()), env);
    }
%>