<%@ page import="com.powerdba.mvc.jsp.*,
                 com.powerdba.mvc.PerfStatManager,
                 com.powerdba.mvc.PerfStatView,
                 com.powerdba.PerfStatDAO,
                 com.powerdba.MenuGenerator,
                 com.powerdba.mvc.PowerDbaActions,
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
    String version = PropertyHolder.getProperty("version");
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

<%@ include  file="powerDbaHeaderInclude.jsp"%>

<left>
<table cellspacing=0 cellpadding=0>

<% if ( myView.getMessage().trim().length() > 0 ) { %>
  <tr>
    <td colspan=10><font size='-1' color='red'><b><%= StringUtility.replace(myView.getMessage()," ","&nbsp;") %></b></font></td>
  </tr>
<% } %>

<% if ( myView.isConnected() ) { %>
  <tr><td>
  <div class='iframeHeaderCell'><a href='' onClick='go(<%=thisForm%>,<%=PowerDbaActions.PERFSTAT%>,"summary");return false;'>Summary</a></div> 
  <div class='iframeHeaderCell'><a href='' onClick='go(<%=thisForm%>,<%=PowerDbaActions.PERFSTAT_DETAIL%>,"<%=PerfStatDAO.CHART%>");return false;'>Chart</a></div> 
  <div class='iframeHeaderCell'><a href='' onClick='go(<%=thisForm%>,<%=PowerDbaActions.PERFSTAT_DETAIL%>,"<%=PerfStatDAO.DATA%>");return false;'>Table</a></div> 
  <td></tr>
<% } %>
                          
<% if ( myView.isConnected() ) { %>
  <tr>
    <td colspan=5><font size='-1'><b>
      &nbsp;&nbsp;<%=myView.getCurrDatabase() + "&nbsp;->&nbsp;" + myView.getTitle()%></b></font>
    </td>
  </tr>
<% } %>


  <tr>
    <td><table><tr>

    <% if ( myView.getCurrAction() == PowerDbaActions.PERFSTAT || myView.getCurrAction() == PowerDbaActions.NEW ) { %>  
      <td><font size=-1>&nbsp;<b>Category:</b></font>
      <%=HtmlComponent.getSelect("stat",myView.getStatList(),myView.getStat(),25,"Select One", null,"smallselect", false)%>
      </td>
      <td>
      <%=HtmlComponent.getSmallButton("Go","","go("+thisForm+","+PowerDbaActions.PERFSTAT+",'summary');return false;",25,15)%>
      </td>
    <% } else { %>
      <td><font size=-1>&nbsp;<b>Category:</font></td>
      <td><%=HtmlComponent.getSelect("stat",myView.getStatList(),myView.getStat(),25,"Select One", null,"smallselect", true)%></b></td>
    <%}%>  
    <td>&nbsp;</td>
    <td><font size=-1>&nbsp;<b>Report Range:</b>&nbsp;</font>
        <%=HtmlComponent.getDateHtml("perfstat","datetime","smallselect",myView.getDateTime().getTime())%></td>  
    <td><%=HtmlComponent.getTimeHtml("perfstat","datetime","smallselect",new Timestamp(myView.getDateTime().getTime()))%></td>
    <td>&nbsp;-&nbsp;</td>
    <td><%=HtmlComponent.getDateHtml("perfstat","datetime2","smallselect",myView.getDateTime2().getTime())%></td>
    <td><%=HtmlComponent.getTimeHtml("perfstat","datetime2","smallselect",new Timestamp(myView.getDateTime2().getTime()))%></td> 

    </tr></table></td>
  </tr>
  
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