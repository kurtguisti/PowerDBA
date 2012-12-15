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
     String version = PropertyHolder.getProperty("version");    
%>

<html>
<head>
   <title>Power*DBA <%=myView.getDatabase()==null?"":" - " + myView.getDatabase().getInstance()%></title>
   <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
   <link REL="StyleSheet" TYPE="text/css" HREF="css/powerdba.css">
   <%@ include  file="powerDbaJavascriptInclude.jsp"%>
   <%= myView.getRefreshMeta() %>
   <link rel="SHORTCUT ICON" href="images/power1_small.jpg">
</head>

<body text="#000000" bgcolor="#ffffff" link="#003399" vlink="#003399" alink="#003399" topmargin="0"
      leftmargin="0" marginheight="0" marginwidth="0" onLoad="<%= myView.getOnLoadActions()%>">

<form method="post" action="powerdba.jsp" name="powerdba">
<input type="hidden" name="formaction" value="<%=myView.getActionOrActionString()%>">
<%= myView.getEnvHiddens() %>
<%@ include  file="powerDbaHeaderInclude.jsp"%>

<left>
<table cellspacing=0 cellpadding=0>

<% if ( myView.getMessage().trim().length() > 0 ) { %>
  <tr><td><font size='-1' color='red'><%= StringUtility.replace(myView.getMessage()," ","&nbsp;") %></font></td></tr>
<% } %>

<% if ( myView.isConnected() ) { %>

  <!-- First Menu Row -->
  <tr>
    <td colspan=2>
      <%= MenuGenerator.getMenu(myView.getCurrAction(), myView.getDatabase(), myView.getSid(), myView.getMenu1()) %>
    </td>
  </tr>  
  
  <!-- Second Menu Row -->
  <tr>
    <td colspan=2>
	  <% if ( myView.getMenu2() != 0 ) { %>
	    <%= MenuGenerator.getMenu(myView.getCurrAction(), myView.getDatabase(), myView.getSid(), myView.getMenu2()) %>
	  <% } %>
    </td>
  </tr>  
  
  <tr>
    <td><img src='images/clear.gif' height=6 width=1 border=0></td>
  </tr>

  <!-- Context / Page Lookups header row -->
  <tr>  
    <td valign=bottom>
      <font size='-1' color=black face='tahoma'><b>
      &nbsp;<%=StringUtility.initCap(myView.getCurrDatabase()) + 
      "&nbsp;&nbsp;<font size=+1 color=gray>&raquo;</font>&nbsp;&nbsp;" + 
      ((myView.getSid()>0)?"Session&nbsp;" + myView.getSid() + 
      "&nbsp;&nbsp;<font size=+1 color=gray>&raquo;</font>&nbsp;&nbsp;":"") +
      myView.getTitle()%></b></font>&nbsp;&nbsp;<a href='<%= myView.getRefreshURL() %>'><img src='images/refreshsmall.jpg' border=0 alt='Refresh this Screen.'></a>
    </td>
    
             <% Tracer.log("After title row", Tracer.DEBUG, "powerdba.jsp"); %>
    
  <% if (  myView.getMenu3() != 0 ) { %>
    <!-- Third Menu Row -->
    <td>
	    <table cellpadding=0 cellspacing=0><tr><td colspan=2>
	    <%= MenuGenerator.getMenu(myView.getCurrAction(), myView.getDatabase(), myView.getSid(), myView.getMenu3()) %>
	    </td></tr></table>
    </td> 
  <% } %>    
  
  <% Tracer.log("After 3rd Menu", Tracer.DEBUG, "powerdba.jsp"); %>
  
  <% if ( myView.getCurrAction() == PowerDbaActions.EXPLAIN_PLAN ) { %>  
    <td><table>
      <tr>
        <th><font size=-2 color=#555555 face='tahoma'>Explain Username:</font></th>
        <td><%=HtmlComponent.getInput("explainusername",15,30,"text",env.getParameter("explainusername"),false,null,"verysmallentry",null)%></td>
        <th><font size=-2 color=#555555 face='tahoma'>Explain Password:</font></th>
        <td><%=HtmlComponent.getInput("explainpassword",15,30,"password",env.getParameter("explainpassword"),false,null,"verysmallentry",null)%></td>
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Re-Explain","","explain(powerdba," + 
                                                                        myView.getHash() + ",\"" + 
                                                                        myView.getCurrDatabase() + "\"," + 
                                                                        myView.getSid() + "); return false;")%>                           
        </td>        
      </tr>
    </table></td>
  <% } else { %>
    <input type="hidden" name="sqltoexplain" value="<%=myView.getEscapedSql()%>">
  <% } %>

  <%if ( myView.getCurrAction() == PowerDbaActions.DB_LOG_HISTORY ) { %>    
    <td>
      <table>
        <tr>
          <td><img src='images/clear.gif' height=1 width=5></td>
          <td><font size=-1 face='tahoma'><b>Report Date:</b></font>
              <%=HtmlComponent.getSelect("rdate",
                                         myView.getDateList(),
                                         myView.getCurrentDate(),
                                         30,
                                         "Select a Date",
                                         "go(powerdba,'powerdba.jsp'," + PowerDbaActions.DB_LOG_HISTORY + ",'" + 
                                             myView.getCurrDatabase() + "','0','0','0'); return false;",
                                         "verysmallentry",
                                         false)%>
          </td>
        </tr>
      </table>
    </td>

  <%} else { %>
    
    <td valign=bottom>
      <table cellspacing=0 cellpadding=0>
        <tr>
          <td valign=bottom><img src='images/clear.gif' width=15 height=1></td>
          
          <% 
            for ( int i=0; i< myView.getLookupGroup().getDisplayTemplate().size(); i++ ) {
              Lookup lookup = (Lookup) myView.getLookupGroup().getDisplayTemplate().get(i);
              Hashtable populatedLists = myView.getLookupGroup().getDisplayGroupData();
              ArrayList selectList = (ArrayList) populatedLists.get(lookup.getName());
              if (selectList == null ) selectList = new ArrayList();
              selectList.addAll(lookup.getPrepopulatedEntries());  
              Collections.sort(selectList);     
              Tracer.log("Getting Select List for " + lookup.getName(), Tracer.DEBUG, "powerdba.jsp");      
              Tracer.log("Length of List is " + selectList.size(), Tracer.DEBUG, "powerdba.jsp"); 
              String currentValue = env.getParameter(lookup.getName());
              if ( currentValue.equals("") ) currentValue = lookup.getInitialValue();  
          %>
	          <td valign=bottom><font size=-2 color=#555555 face='tahoma'><b><%=lookup.getLabel()%></b></font>
	              <%=HtmlComponent.getSelect(lookup.getName(), selectList, currentValue, 30,
	                                         null, "submit();return false;", "smallSelect", false)%>
	          </td>
              <td valign=bottom><img src='images/clear.gif' width=5 height=1></td>
		   <%}%>

        </tr>   
      </table>
    </td>
    
  <%}%> 
  
  </tr>

  </table>
  </left>

  <table width=100% cellpadding=0 cellspacing=0><tr><td>
    <img src=images/clear.gif height=4 width=10></td></tr>
  </table>
  
  <!-- Page Content --> 
  <%=myView.getHtml()%>
  
  <!-- Page Specific Footer -->


  <% if ( myView.getCurrAction() == PowerDbaActions.DB_STREAMS_TRX ) { %>  
    <table>
      <tr>
        <th><font size=-2 color=#555555 face='tahoma'>Oracle Username:</font></th>
        <td><%=HtmlComponent.getInput("streamsusername",15,30,"text",env.getParameter("streamsusername"),false,null,"verysmallentry",null)%></td>
        <th><font size=-2 color=#555555 face='tahoma'>Oracle Password:</font></th>
        <td><%=HtmlComponent.getInput("streamspassword",15,30,"password",env.getParameter("streamspassword"),false,null,"verysmallentry",null)%></td>
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Execute","","managetrx(powerdba,\"" + 
                                               myView.getTrx() + "\",\"" + 
                                               myView.getCurrDatabase() + "\",\"exec\"); return false;")%>                           
        </td> 
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Override","","managetrx(powerdba,\"" + 
                                                                      myView.getTrx() + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"over\"); return false;")%>                           
        </td>  
        
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Delete","","managetrx(powerdba,\"" + 
                                                                      myView.getTrx() + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"del\"); return false;")%>                           
        </td>   
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Override & Delete","","managetrx(powerdba,\"" + 
                                                                           myView.getTrx() + "\",\"" + 
                                                                           myView.getCurrDatabase() + "\",\"overridedelete\"); return false;")%>                           
        </td>       
      </tr>
    </table>
  <% } %>

  <% if ( myView.getCurrAction() == PowerDbaActions.DB_SESS_ZOOM ) { %>  
    <table>
      <tr>
        <th><font size=-2 color=#555555 face='tahoma'>DBA User:</font></th>
        <td><%=HtmlComponent.getInput("dbausername",15,30,"text",env.getParameter("dbausername"),false,null,"verysmallentry",null)%></td>
        <th><font size=-2 color=#555555 face='tahoma'>DBA Password:</font></th>
        <td><%=HtmlComponent.getInput("dbapassword",15,30,"password",env.getParameter("dbapassword"),false,null,"verysmallentry",null)%></td>
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Trace On","","managesess(powerdba,\"" + 
                                                                      myView.getSid() + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"tron\"); return false;")%>                           
        </td> 
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Trace Off","","managesess(powerdba,\"" + 
                                                                      myView.getSid() + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"troff\"); return false;")%>                           
        </td>  
        
        <!--<td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Kill","","managesess(powerdba,\"" + 
                                                                      myView.getSid() + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"kill\"); return false;")%>                           
        </td> -->        
      </tr>
    </table>
  <% } %>

  <% if ( myView.getCurrAction() == PowerDbaActions.DB_CAPTURE_DETAIL ) { %>  
    <table>
      <tr>
        <th><font size=-2 color=#555555 face='tahoma'>Streams Username:</font></th>
        <td><%=HtmlComponent.getInput("streamsusername",15,30,"text",env.getParameter("streamsusername"),false,null,"verysmallentry",null)%></td>
        <th><font size=-2 color=#555555 face='tahoma'>Streams Password:</font></th>
        <td><%=HtmlComponent.getInput("streamspassword",15,30,"password",env.getParameter("streamspassword"),false,null,"verysmallentry",null)%></td>
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Stop","","managecapt(powerdba,\"" + 
                                                                     env.getParameter("key") + "\",\"" + 
                                                                     myView.getCurrDatabase() + "\",\"stop\"); return false;")%>                           
        </td> 
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Start","","managecapt(powerdba,\"" + 
                                                                      env.getParameter("key") + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"start\"); return false;")%>                           
        </td>         
      </tr>
    </table>
  <% } %>

  <% if ( myView.getCurrAction() == PowerDbaActions.DB_JOBS_DETAIL ) { %>  
    <table>
      <tr>
        <th><font size=-2 color=#555555 face='tahoma'>Oracle Username:</font></th>
        <td><%=HtmlComponent.getInput("jobusername",15,30,"text",env.getParameter("jobusername"),false,null,"verysmallentry",null)%></td>
        <th><font size=-2 color=#555555 face='tahoma'>Oracle Password:</font></th>
        <td><%=HtmlComponent.getInput("jobpassword",15,30,"password",env.getParameter("jobpassword"),false,null,"verysmallentry",null)%></td>
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Break","","managejob(powerdba,\"" + 
                                                                     env.getParameter("key") + "\",\"" + 
                                                                     myView.getCurrDatabase() + "\",\"break\"); return false;")%>                           
        </td> 
        <td>&nbsp;&nbsp;
          <%=HtmlComponent.getExtraSmallButton("Unbreak","","managejob(powerdba,\"" + 
                                                                      env.getParameter("key") + "\",\"" + 
                                                                      myView.getCurrDatabase() + "\",\"unbreak\"); return false;")%>                           
        </td>         
      </tr>
    </table>
  <% } %>
  
  
 <!-- Power*DBA Impact Report -->
 <% if ( PropertyHolder.getProperty("showStats").toUpperCase().equals("Y") ) { %>
   <center><table bgcolor=white cellspacing=2><tr><td>  
      <table bgcolor=#cccccc cellspacing=0><tr><td>
        <table cellspacing=0 cellpadding=3>
          <tr>
          <td bgcolor=#eeeeee align=left><font size=-2 color=#555555>&nbsp;Power*DBA Impact:&nbsp;</font></td>
          <% for ( int i=0; i<myView.getPowerDBAStatList().size(); i++ ) { 
             OracleStatistic stat = (OracleStatistic) myView.getPowerDBAStatList().get(i); %>         
             <td bgcolor=#eeeeee><font size=-2 color=#555555>&nbsp;<%=stat.toHtml()%>&nbsp;</font></td>
          <% } %>
          </tr>
        </table>
      </td></tr></table>
    </td></tr></table></center>
  <%}%>



  
<%}%>

</form>

</center>
</body>
</html>

<%
    } catch (WsnException wsn) {
       JspNavigation.toWsnError(wsn, env);
    } catch (Exception e) {
       JspNavigation.toWsnError(new WsnException("powerdba.jsp",e.getMessage()), env);
    }
%>