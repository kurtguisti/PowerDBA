package com.powerdba.gui;

import com.powerdba.OracleStatHolder;
import com.powerdba.OracleStatistic;
import com.powerdba.Query;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.jsp.HtmlComponent;
import com.powerdba.util.StringUtility;
import com.powerdba.util.Tracer;
import com.powerdba.gui.LinkVar;
import com.braju.format.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class PageGenerator {

  private String color1 = "#e5e5e5";
  private String color2 = "#f5f5f5";
  private String headingBackgoundColor = "#73969c";
  private String cellBorderColor = "#cccccc";
  private String font = "smallentry";
  private String detailFont = "arial";
  private String headingClass;
  private String rowClass;
  private String fontSize = "-2";
  private String headingFontSize = "-1";
  private int border = 0;
  private String cellSpacing = "1";
  private String cellPadding = "2";
  private Page page;
  private String jsp = "tctList.jsp";
  private String form;
  private ArrayList selectedKeys = new ArrayList();
  private String database;
  private boolean supressLimitMessage = true;
  private String sql;

  public PageGenerator(Page page) { this.page = page; }
  
  public void setColor1(String value) { this.color1 = value; }
  public void setColor2(String value) { this.color2 = value; }
  public void setFont(String value) { this.font = value; }
  public void setHeadingClass(String value) { this.headingClass = value; }
  public void setRowClass(String value) { this.rowClass = value; }
  public void setFontSize(String value) { this.fontSize = value; }
  public void setHeadingFontSize(String value) { this.headingFontSize = value; }
  public void setCellPadding(String value) { this.cellPadding = value; }
  public void setCellSpacing(String value) { this.cellSpacing = value; }
  public void setBorder(int value) { this.border = value; }
  public void setJsp(String value) { this.jsp = value; }
  public void setForm(String value) {this.form = value; }
  
  public void setSelectedKeys(String[] value) {
    if ( value != null && value.length > 0 ) {
      for ( int i=0; i<value.length; i++ ) {
        selectedKeys.add(value[i]); 
      }
    }      
  }

  public String getHtml() throws Exception {
      
     Tracer.log("Building OLD format HTML from the Page Generator.", Tracer.DEBUG, this);  
     StringBuffer sb = new StringBuffer();
    
     try {        
       
       ArrayList columns = page.getColumns();

       ArrayList row;
       Column column = null;
       String cvalue;
       LinkVar var;
       
       sb.append("<SCRIPT>\n");
       sb.append("function nextpage(myForm) { \n"); 
       sb.append("  myForm.formaction.value = \"" + PowerDbaActions.NEXT_PAGE + "\"\n");                 
       sb.append("  myForm.action = \"" + this.jsp + "\"\n"); 
       sb.append("  myForm.submit(); \n");
       sb.append("}\n");
       sb.append("function prevpage(myForm) { \n"); 
       sb.append("  myForm.formaction.value = \"" + PowerDbaActions.PREVIOUS_PAGE + "\"\n");                 
       sb.append("  myForm.action = \"" + this.jsp + "\"\n"); 
       sb.append("  myForm.submit(); \n");
       sb.append("}\n");
       sb.append("function checkAllBoxes(checked) {                                    \n");                             
       sb.append("  len = document." + this.form + ".elements.length;                  \n");   
       sb.append("  var i=0;                                                           \n");   
       sb.append("  for( i=0; i<len; i++) {                                            \n");                     
       sb.append("    if (document." + this.form + ".elements[i].name=='recordkey') {  \n");                                                     
       sb.append("      document." + this.form + ".elements[i].checked=checked;        \n");   
       sb.append("    }   \n");                                                    
       sb.append("  }\n");   
       sb.append("}\n");   
       sb.append("</SCRIPT>\n");
       
       sb.append("<table cellspacing=0>");
       
       if ( page.getTitle() != null && page.getTitle().length() > 0) {
         sb.append("<tr>");
         sb.append("<th align=left width=150 bgcolor='" + this.getHeadingBackgoundColor() + "'><font size=-1>&nbsp;" + page.getTitle() + "&nbsp;</font></th>\n");
         if ( this.sql != null ) {
           sb.append("<td><a href='sqlPopup.jsp?database=" + this.database + "&query=" + this.sql + "'> sql </a></td>\n");
         }
         sb.append("</tr>");
       }
       
       if ( page.getRows().size() == 0 ) {

         sb.append("<tr><th><font size=-1 color=#444444>&nbsp;No Rows Returned...</font></th></tr>");

       } else {  
       
         sb.append("<tr>");

         if ( page.getTotalPages() > 1 ) {
           sb.append("<td>");
           sb.append("<font size=-1>Page " + page.getPageNumber() + " of " + page.getTotalPages() + "</font>");
           sb.append("</td>");
           sb.append("<td>&nbsp;&nbsp;</td>");
           sb.append("<td>");
           sb.append(HtmlComponent.getImageButton("PrevPage","images/ar_prev.gif",
                                                  "prevpage(" + this.form + 
                                                  ")","Previous Page"));
           sb.append("</td>");
           sb.append("<td>");
           sb.append(HtmlComponent.getImageButton("NextPage","images/ar_next.gif",
                                                  "nextpage(" + this.form + 
                                                  ")","Next Page"));
           sb.append("</td>");
         }
         
         if ( page.isPageSetLimitExceeded() && !this.isSupressLimitMessage() ) {
           sb.append("<td><font color=red>&nbsp;&nbsp;&nbsp;Limit of " + page.getPageSetLimit() + 
                     " rows exceeded, returned only first " + page.getPageSetLimit() + 
                     " rows.  <i>Please refine your search.</i></font></td>");
         }
         
         sb.append("</tr></table>\n");
           
         sb.append("<table ");
         sb.append(" border="      + this.border + 
                   " cellspacing=" + this.cellSpacing + 
                   " cellpadding=" + this.cellPadding +
                   " bgcolor='" + this.getCellBorderColor() + "'>");
  
         // Print column headings...       
         sb.append("<tr class='" + this.headingClass + "'>\n");
         
         // Print Checkbox Heading if a key position is defined
         if ( page.getKeyPosition() > 0 ) {
           sb.append("<td bgcolor=" + this.getHeadingBackgoundColor() + ">\n");
           sb.append("<INPUT title='Check or uncheck all' TYPE='checkbox' ");
           sb.append("name=tctcheckall value=3 onClick='checkAllBoxes(this.checked)' ");
           sb.append("style='height: 15px; width: 15px;'>");
           sb.append("</td>\n");
         }
         
         // Print The Column Headings
         for ( int colIndex=0; colIndex<columns.size(); colIndex++) {

           column = (Column) columns.get(colIndex);

           // Don't print the hidden column heading
           if ( !column.isHidden() ) { 
             sb.append( "<th bgcolor='" + this.getHeadingBackgoundColor() + "'><font size=-1>&nbsp;" + column.getColumnHeading() + "</font>&nbsp;</th>");
           }

         }

         sb.append("</tr>\n");
         
         // Print the Detail Rows
         String rowColor;
         String checkedValue;
         for ( int j=0; j<page.getRows().size(); j++ ) {
         
           row = (ArrayList) page.getRows().get(j);
           
           if ( (j+1) % 2 == 0 ) {
             rowColor = color1;
           } else {
             rowColor = color2;
           }
           
           sb.append("\n<tr bgcolor='" + rowColor + "' class=\"" + this.rowClass + "\">");
           
           // Print the checkbox to select the row
           if ( page.getKeyPosition() > 0 ) {
             sb.append("<td bgcolor='" + rowColor + "'>");
             Column c = new Column();
             c.setClassName("java.math.BigDecimal");
             String keyValue = ObjectTranslator.getString( c, row.get(page.getKeyPosition()-1) );
             checkedValue = "not checked";
             if ( this.selectedKeys.contains(keyValue) ) checkedValue = keyValue;
             if ( keyValue == null ) keyValue = " ";
             sb.append(HtmlComponent.getCheckbox("recordkey", keyValue, checkedValue,false,null,null,false));
             sb.append("</td>");
           }
  
           // Loop through and Print each cell in Row
           for ( int k=0; k<row.size(); k++ ) {
             // Get the column definition for this cell
             column = (Column) columns.get(k);
             
             // Get the String version of the object stored in the cell
             if ( column.isPreformat() ) {
            	 cvalue = "<pre>" + ObjectTranslator.getString(column,row.get(k)) + "</pre>";
             } else {
               cvalue = ObjectTranslator.getString( column, row.get(k) );
             }
             
             
             if ( cvalue == null || cvalue.equals("null") ) cvalue = "&nbsp;";
             
             if ( column.getLink() != null && !cvalue.equals("&nbsp;")) {

               sb.append("<td style='text-align:left;white-space:nowrap;background-color:" + rowColor + "'>");               
               sb.append("<a class='linksmall' ");
               
               if ( column.getLink().getJsFunction() != null ) {
                 sb.append("href='' onClick='" + column.getLink().getJsFunction() + "(");
                 
                 for ( int m=0; m<column.getLink().getVariables().size(); m++ ) {
                   var = (LinkVar) column.getLink().getVariables().get(m);
                   sb.append("\"");
                   sb.append(ObjectTranslator.getString( (Column) columns.get(var.getIndex()), 
                                                         row.get(var.getIndex() )));
                   sb.append("\"");
                   if ( m < column.getLink().getVariables().size() - 1 ) sb.append(",");
                 
                 } 
                 sb.append("); return false;'");
               } else {
                 sb.append("href='" + column.getLink().getFileName());
                 
                 for ( int m=0; m<column.getLink().getVariables().size(); m++ ) {
                   var = (LinkVar) column.getLink().getVariables().get(m);
                 
                   if ( column.getLink().getFileName().indexOf("?") > 0 ) {
                     sb.append("&");
                   } else {
                     if ( m == 0 ) {
                       sb.append("?");
                     } else {
                       sb.append("&");
                     }
                   }
                 
                   sb.append(var.getVarName());
                   sb.append("=");
                   sb.append(ObjectTranslator.getString( (Column) columns.get(var.getIndex()), 
                                                         row.get(var.getIndex() )));
                 }
                 
               }
               sb.append("'>");
               if ( cvalue.length() > 0 ) sb.append("<img src='images/zoom.gif' border=0 vspace=0 width=10 height=10 alt='" +
                                     column.getLink().getDescription() + "'>");
               if ( cvalue.length() > 0 ) sb.append("&nbsp;");
               sb.append(cvalue);
               sb.append("</a>");
               sb.append("</td>\n");
             } else {
               if ( column.isHidden() ) {
                 sb.append(HtmlComponent.getHidden(column.getColumnName().toLowerCase(), cvalue));
               } else {
                 sb.append("<td style='font: 9px " + detailFont + ";background-color:" + rowColor + "'>&nbsp;");
                 sb.append(cvalue);
                 sb.append("</td>\n");                     
               }
             }
           }
           sb.append( "</tr>\n");
         }
         
       }
       sb.append("</table>\n");
    
     } catch ( Exception e ) {
         Tracer.log(e, "Exception: Error in generating page html", Tracer.ERROR, "");
         throw e;
     }    
     return sb.toString();
	}
  
  public String getHtmlNew() throws Exception {
      
     Tracer.log("Building NEW format HTML from the Page Generator.  Database Descriptor is " + this.database, Tracer.DEBUG, this);   
     StringBuffer sb = new StringBuffer();
    
     try {        
       
       ArrayList columns = page.getColumns();

       Column column = null;
       String cvalue;
       LinkVar var;
       
       sb.append("<SCRIPT>\n");
       sb.append("function nextpage(myForm) { \n"); 
       sb.append("  myForm.formaction.value = \"" + PowerDbaActions.NEXT_PAGE + "\"\n");                 
       sb.append("  myForm.action = \"" + this.jsp + "\"\n"); 
       sb.append("  myForm.submit(); \n");
       sb.append("}\n");
       sb.append("function prevpage(myForm) { \n"); 
       sb.append("  myForm.formaction.value = \"" + PowerDbaActions.PREVIOUS_PAGE + "\"\n");                 
       sb.append("  myForm.action = \"" + this.jsp + "\"\n"); 
       sb.append("  myForm.submit(); \n");
       sb.append("}\n");
       sb.append("function checkAllBoxes(checked) {                                    \n");                             
       sb.append("  len = document." + this.form + ".elements.length;                  \n");   
       sb.append("  var i=0;                                                           \n");   
       sb.append("  for( i=0; i<len; i++) {                                            \n");                     
       sb.append("    if (document." + this.form + ".elements[i].name=='recordkey') {  \n");                                                     
       sb.append("      document." + this.form + ".elements[i].checked=checked;        \n");   
       sb.append("    }   \n");                                                    
       sb.append("  }\n");   
       sb.append("}\n");   
       sb.append("</SCRIPT>\n");
   
       sb.append("<table cellspacing=0>");  
       sb.append("<tr>");   
       
       if ( page.getTitle() != null ) {
         sb.append("<th align=left width=150 bgcolor='" + this.getHeadingBackgoundColor() + "'><font size=-1>&nbsp;" + 
                   page.getTitle() + "&nbsp;</font>&nbsp;&nbsp;<font size=-3>"+page.getGenTime()+"ms</font></th>\n");
         sb.append("<td><table cellpadding=0 cellspacing=0><tr>");          
         for ( int i=0; i<page.getCharts().size(); i ++ ) {
           String chartName = (String) page.getCharts().get(i);
           sb.append("<td>&nbsp;<img src=images/chart.jpg width=16 height=16 border=0 alt='"+chartName+"'></td>");
         }
         //if ( this.sql != null ) {
         //  sb.append(HtmlComponent.getSmallButton("Show SQL", "", "sqlpop("+this.database+","+this.getSql()+"); return false;", 90, 15) + "\n");
         //}
         sb.append("</tr></table></td>");
       }  
       sb.append("</tr>");
       
       if ( page.getErrorMessage() != null && page.getErrorMessage().length() <= 132 ) {       
         sb.append("<tr><th><font size=-2 color=red>" + page.getErrorMessage() + "</font></th></tr>");
       } else if ( page.getErrorMessage() != null && page.getErrorMessage().length() > 132 ) {
         sb.append("<tr><th><font size=-2 color=red>SQL Error building page.</font></th></tr>");
       } else if ( page.getErrorMessage() == null && page.getRows().size() == 0 ) {
         sb.append("<tr><th><font size=-2 color=#444444>&nbsp;No Rows Returned...</font></th></tr>");
       } else if ( page.getErrorMessage() == null && page.getRows().size() > 0 ) {  
       
         sb.append("<tr>");

         if ( page.getTotalPages() > 1 ) {
           sb.append("<td>");
           sb.append("<font size=-1>Page " + page.getPageNumber() + " of " + page.getTotalPages() + "</font>");
           sb.append("</td>");
           sb.append("<td>&nbsp;&nbsp;</td>");
           sb.append("<td>");
           sb.append(HtmlComponent.getImageButton("PrevPage","images/ar_prev.gif",
                                                  "prevpage(" + this.form + 
                                                  ")","Previous Page"));
           sb.append("</td>");
           sb.append("<td>");
           sb.append(HtmlComponent.getImageButton("NextPage","images/ar_next.gif",
                                                  "nextpage(" + this.form + 
                                                  ")","Next Page"));
           sb.append("</td>");
         }
         
         if ( page.isPageSetLimitExceeded() && !this.isSupressLimitMessage()) {
           sb.append("<td><font color=red>&nbsp;&nbsp;&nbsp;Limit of " + page.getPageSetLimit() + 
                     " rows exceeded, returned only first " + page.getPageSetLimit() + 
                     " rows.  <i>Please refine your search.</i></font></td>");
         }
         
         sb.append("</tr></table>\n");
           
         sb.append("<table ");
         sb.append(" border="      + this.border + 
                   " cellspacing=" + this.cellSpacing + 
                   " cellpadding=" + this.cellPadding +
                   " bgcolor='"    + this.getCellBorderColor() + "'>");
  
         // Print column headings...       
         sb.append("<tr class='" + this.headingClass + "'>\n");
         
         // Print Checkbox Heading if a key position is defined
         if ( page.getKeyPosition() > 0 ) {
           sb.append("<td bgcolor=" + this.getHeadingBackgoundColor() + ">\n");
           sb.append("<INPUT title='Check or uncheck all' TYPE='checkbox' ");
           sb.append("name=tctcheckall value=3 onClick='checkAllBoxes(this.checked)' ");
           sb.append("style='height: 15px; width: 15px;'>");
           sb.append("</td>\n");
         }
         
         // Print The Column Headings
         for ( int colIndex=0; colIndex<columns.size(); colIndex++) {
           column = (Column) columns.get(colIndex);
           // Don't print the hidden column heading
           if ( !column.isHidden() ) { 
             sb.append( "<th align=center bgcolor='" + this.getHeadingBackgoundColor() + "'><font size=-1>&nbsp;" + column.getColumnHeading() + "</font>&nbsp;</th>");
           }
         }
         sb.append("</tr>\n");
         
         // Print the Detail Rows
         Hashtable row;
         String rowColor;
         String checkedValue;

         // Loop through the Rows
         for ( int j=0; j<page.getRows().size(); j++ ) {   // row loop
                  
           row = (Hashtable) page.getRows().get(j);
           
           if ( (j+1) % 2 == 0 ) {
             rowColor = color1;
           } else {
             rowColor = color2;
           }
           
           sb.append("\n<tr bgcolor='" + rowColor + "' class=\"" + this.rowClass + "\">");
           
           // Print the checkbox to select the row
           if ( page.getKeyPosition() > 0 ) {
             sb.append("<td bgcolor='" + rowColor + "'>");
             Column c = new Column();
             c.setClassName("java.math.BigDecimal");
             String keyValue = ObjectTranslator.getStringNew( c, row );
             checkedValue = "not checked";
             if ( this.selectedKeys.contains(keyValue) ) checkedValue = keyValue;
             if ( keyValue == null ) keyValue = " ";
             sb.append(HtmlComponent.getCheckbox("recordkey", keyValue, checkedValue,false,null,null,false));
             sb.append("</td>");
           }
  
           // Print each Field in Row
           for ( int k=0; k<row.size(); k++ ) {   // column loop
           
             // Get the column definition for this cell
             column = (Column) columns.get(k);
             String just = null;
             switch ( column.getJustification() ) {
               case Column.LEFT:
                 just = "left";
                 break;
               case Column.RIGHT:
                 just = "right";
                 break;
               case Column.CENTER:
                 just = "center";
                 break;
             }   
           
             
             // Get the String version of the object stored in the cell, wrap in pre tag if specified in sql xml.
             if ( column.isPreformat() ) {
            	 cvalue = "<pre>" + StringUtility.encodeXml(ObjectTranslator.getStringNew(column, row)) + "</pre>";
             } else {
               cvalue = ObjectTranslator.getStringNew( column, row );
             }
             
             if ( this.page.getDataType() == Query.STATISTICS ) {
               switch (k) {
                 case 0:  // class value
                   String originalValue = cvalue;
                   cvalue = originalValue + "-" + ((String) new OracleStatistic().getStatClasses().get(new Integer(cvalue)));
                   if ( cvalue == null ) cvalue = originalValue; 
                   break;
                    
                 case 2: // value value
                   // Get the stat definition from the xml
                   Column c = (Column) page.getColumnsHash().get("name");
                   String statName = ObjectTranslator.getStringNew(c, row);
                   OracleStatistic os = OracleStatHolder.getStatDef(statName);
                   if ( os == null ) {
                     Tracer.log("STAT NOT IN XML!!!! " + statName, Tracer.WARNING, "");
                     os = new OracleStatistic(statName, Long.parseLong(cvalue), 99999, null, 1, ""); 
                   }
                   // Set the current value of it.
                   if ( cvalue != null ) {
                     os.setValue(Long.parseLong(cvalue));
                   } else {
                     os.setValue(0);
                   }
                   cvalue = os.getAdjustedValue();
                   break;
               }
             }
                        
             if ( cvalue == null || cvalue.equals("null") ) cvalue = "&nbsp;";
             
             if ( column.getLink() != null && !cvalue.equals("&nbsp;")) {

               sb.append("<td align='" + just + "' style='text-align:left;white-space:nowrap;background-color:" + rowColor + "'>");               
               sb.append("<a class='linksmall' onMouseOver=\"window.status='Click to get More Information'\" ");
               
               if ( column.getLink().getJsFunction() != null ) {
                // Java Script
                 sb.append("href='' onClick='" + column.getLink().getJsFunction() + "(");
                 
                 for ( int m=0; m<column.getLink().getVariables().size(); m++ ) {

                   var = (LinkVar) column.getLink().getVariables().get(m);
                   sb.append("\"");
                   // go get the string value for the object sitting in the column
                   sb.append(ObjectTranslator.getStringNew( (Column) columns.get(var.getIndex()),row));
                   sb.append("\"");
                   if ( m < column.getLink().getVariables().size() - 1 ) sb.append(",");
                 
                 } 
                 sb.append("); return false;'");
                 
               } else {
                 //href
                 sb.append("href='powerdba.jsp?formaction=" + column.getLink().getAction() + "&database=" + this.database);
                 
                 for ( int m=0; m<column.getLink().getVariables().size(); m++ ) {
                 
                   var = (LinkVar) column.getLink().getVariables().get(m);

                   sb.append("&");                 
                   sb.append(var.getVarName());
                   sb.append("=");
                   
                   if ( page.getColumnsHash().size() == 0 ) throw new SQLException("columns hash size is 0");
                   
                   Column c = (Column) page.getColumnsHash().get(var.getColumnName().toLowerCase());
                   
                   if ( c==null ) throw new SQLException("Column definition for " + var.getColumnName().toLowerCase() + 
                                                         " length: " + var.getColumnName().length() + " was found to be " +
                                                         " null trying to get value to put in the link variable " + var.getVarName() + ".");

                   sb.append(ObjectTranslator.getStringNew(c, row).replaceAll("'", ""));
                 }
                 
               }
               sb.append("'>");

               String altText = null;
               if ( column.getLink().getGetAltFrom() != null ) {
	               Column c2 = (Column) page.getColumnsHash().get(column.getLink().getGetAltFrom().toLowerCase());
	               if ( c2==null ) throw new SQLException("Column definition for " + column.getLink().getGetAltFrom().toLowerCase() + 
	                                                      " was found to be null.");
	               altText = ObjectTranslator.getStringNew(c2, row);  
               } else {
	               altText = column.getLink().getDescription();
               }
               
               if ( cvalue.length() > 0 ) {
                 sb.append("<img src='images/zoom.gif' border=0 vspace=0 width=10 height=10 alt='" +
                           column.getLink().getDescription() + "'>&nbsp;");
               }
               sb.append(cvalue);
               sb.append("</a>");
               sb.append("</td>\n");
             } else {
               if ( column.isHidden() ) {
                 sb.append(HtmlComponent.getHidden(column.getColumnName().toLowerCase(), cvalue));
               } else {
                 sb.append("<td align='" + just + "' style='font: 9px " + detailFont + ";background-color:" + rowColor + "'>&nbsp;");
                 sb.append(cvalue);
                 sb.append("</td>\n");                     
               }
             }
           }
           sb.append( "</tr>\n");

         }
         
       }
       sb.append("</table>\n");
    
     } catch ( Exception e ) {
         Tracer.log(e, "Exception: Error in generating page html", Tracer.ERROR, "");
         throw e;
     }    
     return sb.toString();
	}
  
  public String getAscii() throws Exception {
    
    Tracer.log("Building ASCII output from the Page Generator.  Database Descriptor is " + this.database, Tracer.DEBUG, this);   
    StringBuffer sb = new StringBuffer();
   
    try {        
      
      ArrayList columns = page.getColumns();

      Column column = null;
      String cvalue;
      StringBuffer underline = new StringBuffer();
      
      if ( page.getTitle() != null ) {
        sb.append(page.getTitle()+"\n");
      	for ( int i=0; i< page.getTitle().length(); i++ ) {
      		underline.append("-");
      	}
        sb.append(underline + "\n");
      }  
      
      if ( page.getErrorMessage() != null ) {       
        sb.append(page.getErrorMessage() + "\n");
      } else if ( page.getRows().size() == 0 ) {
        sb.append("No Rows Returned...");
      } else {  
        if ( page.getTotalPages() > 1 ) {
          sb.append("Page " + page.getPageNumber() + " of " + page.getTotalPages());
        }
        
        if ( page.isPageSetLimitExceeded() && !this.isSupressLimitMessage()) {
          sb.append("Limit of " + page.getPageSetLimit() + " rows exceeded, returned only first " + page.getPageSetLimit() + 
                    " rows.  Please refine your search.");
        }  

        // Print The Column Headings
        Parameters printRow = new Parameters();
        Parameters printUnderlines = new Parameters();
        StringBuffer printFormat = new StringBuffer();
        
        for ( int colIndex=0; colIndex<columns.size(); colIndex++) {
          column = (Column) columns.get(colIndex);
          // Don't print the hidden column heading
          if ( !column.isHidden() ) { 
          	// Add the heading text values to the Parameters List
          	printRow.add(column.getColumnHeading());
          	if ( column.getColumnHeading().length() > column.getMaxLength() ) {
          		column.setMaxLength(column.getColumnHeading().length());
          	}
          	
          	// Build the print format from the Columns definition in the page
          	printFormat.append("%");
          	if ( column.getJustification()== Column.LEFT ) {
          		printFormat.append("-"); // the minus sign left justifies the column.
          	}
          	printFormat.append(column.getMaxLength() + "s ");
          	
          	underline.setLength(0);
          	for ( int i=0; i< column.getMaxLength(); i++ ) {
          		underline.append("-");
          	}
          	printUnderlines.add(underline.toString());
          }
        }
        printFormat.append("\n");
        
        sb.append(Format.sprintf(printFormat.toString(), printRow));
        sb.append(Format.sprintf(printFormat.toString(), printUnderlines));
        Tracer.log("Print Format: " + printFormat, Tracer.DEBUG, this);
        
        // Print the Detail Rows
        Hashtable row;

        // Loop through the Rows
        for ( int j=0; j<page.getRows().size(); j++ ) {   // row loop
                 
          row = (Hashtable) page.getRows().get(j);
 
          // Print each Field in Row
          printRow = new Parameters();
          for ( int k=0; k<row.size(); k++ ) {   // column loop
          
            // Get the column definition for this cell
            column = (Column) columns.get(k);
            
            // Get the String version of the object stored in the cell
            cvalue = ObjectTranslator.getStringNew( column, row );
            
            if ( this.page.getDataType() == Query.STATISTICS ) {
               switch (k) {
                 case 0:  // class value
                   String originalValue = cvalue;
                   cvalue = originalValue + "-" + ((String) new OracleStatistic().getStatClasses().get(new Integer(cvalue)));
                   if ( cvalue == null ) cvalue = originalValue; 
                   break;
                   
                 case 2: // value value
                   // Get the stat definition from the xml
                   Column c = (Column) page.getColumnsHash().get("name");
                   String statName = ObjectTranslator.getStringNew(c, row);
                   OracleStatistic os = OracleStatHolder.getStatDef(statName);
                   if ( os == null ) {
                     Tracer.log("STAT NOT IN XML!!!! " + statName, Tracer.WARNING, "");
                     os = new OracleStatistic(statName, Long.parseLong(cvalue), 99999, null, 1, ""); 
                   }
                   // Set the current value of it.
                   if ( cvalue != null ) {
                     os.setValue(Long.parseLong(cvalue));
                   } else {
                     os.setValue(0);
                   }
                   cvalue = os.getAdjustedValue();
                   break;
               }
            }
                        
            if ( cvalue == null || cvalue.equals("null") ) cvalue = "NA";
          	printRow.add(cvalue);
            //sb.append(cvalue + "\t");                    
          }
          sb.append(Format.sprintf(printFormat.toString(), printRow));
        }

      }
   
    } catch ( Exception e ) {
        Tracer.log(e, "Exception: Error in generating page ascii output", Tracer.ERROR, "");
        throw e;
    }    
    return sb.toString();
	}



  public void setDetailFont(String detailFont)
  {
    this.detailFont = detailFont;
  }


  public String getDetailFont()
  {
    return detailFont;
  }


  public void setHeadingBackgoundColor(String headingBackgoundColor)
  {
    this.headingBackgoundColor = headingBackgoundColor;
  }


  public String getHeadingBackgoundColor()
  {
    return headingBackgoundColor;
  }


  public void setDatabase(String database)
  {
    this.database = database;
  }


  public String getDatabase()
  {
    return database;
  }


  public void setCellBorderColor(String cellBorderColor)
  {
    this.cellBorderColor = cellBorderColor;
  }


  public String getCellBorderColor()
  {
    return cellBorderColor;
  }


  public void setSupressLimitMessage(boolean supressLimitMessage)
  {
    this.supressLimitMessage = supressLimitMessage;
  }


  public boolean isSupressLimitMessage()
  {
    return supressLimitMessage;
  }

	/**
	 * @return Returns the sql.
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql The sql to set.
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}


}