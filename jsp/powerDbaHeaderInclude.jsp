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
                <td>
                  <table border=0 cellpadding=0 cellspacing=0>
                    <tr> 
                      <td>

                        <table border=0 cellpadding=0 cellspacing=0>
                          <tr>

  <td>
    <%=HtmlComponent.getSelect("database", 
                               myView.getShortDbList(),
                               myView.isConnected()?env.getParameter("database"):"", 
                               30, 
                               "Select a Database", 
                               "submit()", 
                               "smallSelectBold", 
                               false,
                               "+ ")%>
  </td>

<% if ( myView.isConnected() ) { %>
  <td><img+ src='images/clear.gif' width=10 height=1></td>
  <td><a href='<%= myView.getOnlyLocalRefreshURL() %>'><img src='images/collapse.jpeg' border=0 alt='Show selected instance ONLY.'></a></td>
  <td><img src='images/clear.gif' width=5 height=1></td>
  <td><a href='<%= myView.getAllInstancesRefreshURL() %>'><img src='images/expand.jpeg' border=0 alt='Expand to show ALL instances of this database.'></a></td>
  <td><img src='images/clear.gif' width=20 height=1></td>
  <td><%= MenuGenerator.getMenu(myView.getCurrAction(), myView.getDatabase(), MenuGenerator.TOP_MENU) %></td> 
  <td><img src='images/clear.gif' width=20 height=1></td>
  <td class=smallentry><b>Refresh:</b></td>
  <td><%=HtmlComponent.getSelect("refreshinterval",
                                 myView.getRefreshIntervalList(),
                                 env.getParameter("refreshinterval"), 10, null,
                                 "submit()",
                                 "smallSelect",
                                 false)%>
  </td>

<% } %>
<td><img src='images/clear.gif' width=10 height=1></td>
<td><%=HtmlComponent.getExtraSmallButton("Extended Actions","extendedSitemap.jsp","")%></td>
<td><img src='images/clear.gif' width=15 height=1></td>
<td><%=HtmlComponent.getExtraSmallButton("Current Database Status","dbStatus.jsp","")%><td>
<td><img src='images/clear.gif' width=35 height=1></td>

</tr></table></td>
</tr>



<tr>
<td colspan=12><table border=0 cellpadding=0 cellspacing=0>
<tr>

<% if ( myView.isConnected() ) { %>

  <td valign='bottom'><font size=-2 color='#333333'><b>Inst:</b>&nbsp;<%=myView.getDatabase().getInstance().toLowerCase()%></td>
  <td valign='bottom'>&nbsp;</td>
  <td valign='bottom'><font size=-2 color='#333333'><b>DB:</b>&nbsp;<%=myView.getDatabase().getDatabase().toLowerCase()%></font></td>
  <!-- <td valign='bottom'>&nbsp;</td>
  <td colspan=3 valign='bottom'><font size=-2 color='#000000'>&nbsp;<b>Server Time:</b></b>&nbsp;<font size=-2 color='#333333'><%=myView.getCurrentTime()%></td>  -->
  <td valign='bottom'>&nbsp;</td>
  <td colspan=3 valign='bottom'><font size=-2 color='#000000'>
  &nbsp;<b>Version:</b><font size=-2 color='#333333'> <%= myView.getDatabase().getStringVersion() %></td>  
  <td valign='bottom'>&nbsp;</td>
  <td colspan=3 valign='bottom'><font size=-2 color='#000000'>
  &nbsp;<b>Compatible:</b><font size=-2 color='#333333'> <%= myView.getDatabase().getCompatible() %></td>  
  <% if ( myView.getDatabase().isCluster() ) { %>
    <td valign='bottom'>&nbsp;</td>
    <td colspan=3 valign='bottom'><font size=-2 color='#000000'>&nbsp;<b>All Instances:</b></b>&nbsp;<font size=-2 color='#333333'><%=myView.getHTMLForInstanceLinks(myView.getDatabase().getOtherRacDescriptors())%></td> 
  <% } %>
  <td><img src='images/clear.gif' width=20 height=1></td>
  <td valign='bottom'><font size=-2 color=#777777><%= myView.getRunTime()%>ms</font></td>
    <td></td>
<% } else { %>
  <td></td>
<% } %>
  
</tr></table></td>
</tr>
</table>
</td>

</tr>

</table></td></tr>
</table></center></td></tr>
<tr>
<td bgcolor=black><img src='images/clear.gif' height=1></td>
</tr>
</table>
