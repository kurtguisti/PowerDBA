

package com.powerdba.mvc.jsp;

import com.powerdba.util.Tracer;
import com.powerdba.util.DateTranslator;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class HtmlComponent {

    private ArrayList mins;
    private ArrayList hrs;
    private ArrayList am;
    private static String OBJECT_NAME = "HtmlComponent";

    // TABLE - SECTION
    // TableOpen for a black bordered HTML table
    public static String getBlackBoxTableOpen(int width) {

        // the first part of a box with thin black border around contents
        StringBuffer tableOpen = new StringBuffer(250);

        tableOpen.append(
            "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\""
            + width + "\">");
        tableOpen.append(
            "<tr><td colspan=\"3\" bgcolor=\"black\"><img src=\"../images/clear.gif\"></td></tr>");
        tableOpen.append(
            "<tr><td bgcolor=\"black\"><img src=\"../images/clear.gif\"></td>");
        tableOpen.append("<td align=\"center\" valign=\"center\" width=\""
                         + (width - 4) + "\">");

        return tableOpen.toString();
    }

    // TableClose for a black bordered HTML table
    public static String getBlackBoxTableClose() {

        // the second(last) part of a box with thin black border around contents
        StringBuffer tableClose = new StringBuffer(160);

        tableClose.append(
            "</td><td bgcolor=\"black\"><img src=\"../images/clear.gif\"></td>");
        tableClose.append(
            "</tr><tr><td colspan=\"3\" bgcolor=\"black\"><img src=\"../images/clear.gif\"></td>");
        tableClose.append("</tr></table>");

        return tableClose.toString();
    }

    // FORM - Section
    // Formopen simplifies the main portion of our form opens into a few params to be sent in.
    public static String buildFormOpen(String url, String name,
                                       String target, String action,
                                       String source, String mode) {

        StringBuffer formOpen = new StringBuffer(300);

        formOpen.append("<form method=\"post\" action=\"" + url
                        + "\" name=\"" + name + "\">");
        formOpen.append("<input type=\"hidden\" name=\"formaction\" value=\""
                        + action + "\">");
        formOpen.append("<input type=\"hidden\" name=\"target\" value=\""
                        + target + "\">");
        formOpen.append("<input type=\"hidden\" name=\"source\" value=\""
                        + source + "\">");
        formOpen.append("<input type=\"hidden\" name=\"mode\" value=\""
                        + mode + "\">");

        return formOpen.toString();
    }

    // BUTTON - SECTION
    // create an image button that accepts onclick logic
    public static String getImageButton(String name, String image,
                                        String onClick, String alt) {

        return "<a href=\"\" onClick=\"" + onClick + "; return false;\"><img SRC=\"" + image
                + "\" ALT=\"" + alt + "\" border=0></a>";
    }
    
    public static String getImageButtonHref(String name, String image,
            String href, String alt) {

			return "<a href=\"" + href + "\" ><img SRC=\"" + image
			+ "\" ALT=\"" + alt + "\" border=0></a>";
    }

    public static String getImageSubmit(String name, String image, String alt) {

        // wrap our preferences into the normal button

              return "<input type=image name=\"" + name + "\" SRC=\"../images/" + image
                      + "\" ALT=\"" + alt + "\" border=0></a>";


              //return "<input type=\"image\" src=\"../images/" + image + "\" name=\"" + name
              //         + "\" onClick=\"" + onClick + "\" border=\"0\">";
        // temp button return
        //return "<input type=\"button\" value=\"" + name + "\" name=\"" + name
         //      + "\" onClick=\"" + onClick + "\" border=\"0\">";
    }

    // BUTTON - SECTION
    // create an image button that accepts onclick logic
    public static String getFormButton(String name, String windowStatus,
                                       String onClick, int width,
                                       int height) {

        // wrap our preferences into the normal button
        return getButton(name, "",
                         " onMouseOver=\"window.status='" + windowStatus
                         + "'; return true;\" onClick=\"" + onClick
                             + "; return false;\"", width, height);
    }

    public static String getSmallFormButton(String name, String windowStatus,
                                            String onClick, int width,
                                            int height) {

        // wrap our preferences into the normal button
        return getSmallButton(name, ""," onMouseOver=\"window.status='" + windowStatus
                              + "'; return true;\" onClick=\"" + onClick
                              + "; return false;\"", width, height);
    }

    // more complex button with more features
    public static String getButton(String name, String href,
                                   String javaScript, int width, int height) {

        StringBuffer buttonStr = new StringBuffer(350);

        buttonStr.append(
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\" width=\""
            + width + "\" height=\"" + height + "\">");
        buttonStr.append("<tr align=\"center\" valign=\"center\">");
        buttonStr.append("<td bgcolor=\"#111111\"><a href=\"" + href + "\" "
                         + javaScript + " class=\"littlewhite\">");
        buttonStr.append(name + "</a></td></tr></table>");

        return buttonStr.toString();
    }

    public static String getSmallButton(String linkText, String href, String js, int width, int height) {

        StringBuffer bs = new StringBuffer(350);

        bs.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\" width=\""+ width + "\" height=\"" + height + "\">");
        bs.append("<tr align=\"center\" valign=\"center\">");
        bs.append("<td bgcolor='#777777'>");
        bs.append("<a class='linkSmallWhite' href=\"" + href + "\" onClick=\"" + js + "\">");
        bs.append(linkText + "</a>");
        bs.append("</td></tr></table>");

        return bs.toString();
    }
    
    public static String getSmallButtonReadOnly(String linkText, int width, int height) {

        StringBuffer bs = new StringBuffer(350);

        bs.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\" width=\""+ width + "\" height=\"" + height + "\">");
        bs.append("<tr align=\"center\" valign=\"center\">");
        //bs.append("<td background=\"images/tdbg2.gif\">");
        bs.append("<td bgcolor='#777777'><font size=-1 color=white><b>");
        bs.append(linkText);
        bs.append("</b></font></td></tr></table>");

        return bs.toString();
    }

    public static String getTextArea(String name, String text, int columns, int rows) {

      StringBuffer bs = new StringBuffer();
      bs.append("<font size=-1><textarea name =" + name + " rows=" + rows + " " + "cols=" + columns + ">");
      bs.append(text);
      bs.append("</textarea></font>");
      return bs.toString();

    }
    
    public static String getExtraSmallButton(String name, String href, String javaScript) {
        return getExtraSmallButton(name, href, javaScript, "#003399");
    }

    public static String getExtraSmallButton(String name, String href, String javaScript, String color) {

        StringBuffer bs = new StringBuffer(350);
        bs.append("<a class='" + "linkSmallWhite" + "' href='" + href + "' " + "onClick='" + javaScript + "'><font size=-2 style='verdana' color='" + color + "'>");
        bs.append(name + "</font></a>");

        return bs.toString();
    }

    public static String getButtonGray(String name, int width, int height) {

        StringBuffer buttonStr = new StringBuffer(350);

        buttonStr.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\" width=\"" + width + "\" height=\"" + height + "\">");
        buttonStr.append("<tr align=\"center\" valign=\"center\">");
        buttonStr.append("<td bgcolor=\"#aaaaaa\"><div class=\"littlewhite\"><b>");
        buttonStr.append(name + "</a></b></td></tr></table>");

        return buttonStr.toString();
    }

    /**
     * more complex button with more features and no wrapping
     */
    public static String getButtonNoWrap(String name, String href,
                                         String javaScript, int width,
                                         int height) {

        StringBuffer buttonStr = new StringBuffer(350);

        buttonStr.append(
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"");
        buttonStr.append(width);
        buttonStr.append("\" height=\"");
        buttonStr.append(height);
        buttonStr.append("\">");
        buttonStr.append("<tr align=\"center\" valign=\"center\">");
        buttonStr.append(
            "<td rowspan=2 width=1 height=" + height
            + " bgcolor=\"#000000\"><img src=\"/wsn/images/clear.gif\" width=1 height="
            + height + "></td>");
        buttonStr.append(
            "<td width=" + width
            + " height=1 bgcolor=\"#000000\"><img src=\"/wsn/images/clear.gif\" width="
            + width + " height=1></td></tr>");
        buttonStr.append("<tr align=\"center\" valign=\"center\">");
        buttonStr.append("<td bgcolor=\"#000000\" nowrap><a href=\"");
        buttonStr.append(href);
        buttonStr.append("\" ");
        buttonStr.append(javaScript);
        buttonStr.append(" class=\"little\">");
        buttonStr.append(name);
        buttonStr.append("</a></td></tr></table>");

        return buttonStr.toString();
    }

    /**
     * more complex button with more features and no wrapping but no link
     */
    public static String getButtonNoWrapGray(String name, int width,
                                             int height) {

        StringBuffer buttonStr = new StringBuffer(350);

        buttonStr.append(
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"");
        buttonStr.append(width);
        buttonStr.append("\" height=\"");
        buttonStr.append(height);
        buttonStr.append("\">");
        buttonStr.append("<tr align=\"center\" valign=\"center\">");
        buttonStr.append(
            "<td rowspan=2 width=1 height=" + height
            + " bgcolor=\"#CCCCCC\"><img src=\"/wsn/images/clear.gif\" width=1 height="
            + height + "></td>");
        buttonStr.append(
            "<td width=" + width
            + " height=1 bgcolor=\"#CCCCCC\"><img src=\"/wsn/images/clear.gif\" width="
            + width + " height=1></td></tr>");
        buttonStr.append("<tr align=\"center\" valign=\"center\">");
        buttonStr
            .append("<td bgcolor=\"#CCCCCC\" nowrap><div class=\"little\">");
        buttonStr.append(name);
        buttonStr.append("</div></td></tr></table>");

        return buttonStr.toString();
    }


    public static String getCountryMenu(String selectName) {

        StringBuffer country = new StringBuffer(5000);

        country.append("<select name=\"" + selectName + "\">");
        country.append(
            "<option> Albania<option> Algeria<option> American Samoa<option> Andorra<option> Angola<option> Anguilla");
        country.append(
            "<option> Antarctica <option> Antigua And Barbuda<option> Argentina<option> Armenia<option> Aruba<option> Australia");
        country.append(
            "<option> Austria<option> Azerbaijan<option> Bahamas<option> Bahrain<option> Bangladesh<option> Barbados");
        country.append(
            "<option> Belarus<option> Belgium<option> Belize<option> Benin<option> Bermuda<option> Bhutan<option> Bolivia");
        country.append(
            "<option> Bosnia and Herzegowina<option> Botswana<option> Bouvet Island<option> Brazil");
        country.append(
            "<option> British Indian Ocean Territory<option> Brunei Darussalam<option> Bulgaria<option> Burkina Faso");
        country.append(
            "<option> Burma<option> Burundi<option> Cambodia<option> Cameroon<option> Canada<option> Cape Verde");
        country.append(
            "<option> Cayman Islands<option> Central African Republic<option> Chad<option> Chile<option> China");
        country.append(
            "<option> Christmas Island<option> Cocos (Keeling) Islands<option> Colombia<option> Comoros<option> Congo");
        country.append(
            "<option> Congo, the Democratic Republic of the<option> Cook Islands<option> Costa Rica<option> Cote d'Ivoire");
        country.append(
            "<option> Croatia<option> Cyprus<option> Czech Republic<option> Denmark<option> Djibouti<option> Dominica");
        country.append(
            "<option> Dominican Republic<option> East Timor<option> Ecuador<option> Egypt<option> El Salvador<option> England");
        country.append(
            "<option> Equatorial Guinea<option> Eritrea<option> Espana<option> Estonia<option> Ethiopia<option> Falkland Islands");
        country.append(
            "<option> Faroe Islands<option> Fiji<option> Finland<option> France<option> French Guiana<option> French Polynesia");
        country.append(
            "<option> French Southern Territories<option> Gabon<option> Gambia<option> Georgia<option> Germany<option> Ghana");
        country.append(
            "<option> Gibraltar<option> Great Britain<option> Greece<option> Greenland<option> Grenada<option> Guadeloupe");
        country.append(
            "<option> Guam<option> Guatemala<option> Guinea<option> Guinea-Bissau<option> Guyana<option> Haiti");
        country.append(
            "<option> Heard and Mc Donald Islands<option> Honduras<option> Hong Kong<option> Hungary<option> Iceland<option> India");
        country.append(
            "<option> Indonesia<option> Ireland<option> Israel<option> Italy<option> Jamaica<option> Japan<option> Jordan");
        country.append(
            "<option> Kazakhstan<option> Kenya<option> Kiribati<option> Korea, Republic of<option> Korea (South)<option> Kuwait");
        country.append(
            "<option> Kyrgyzstan<option> Lao People's Democratic Republic<option> Latvia<option> Lebanon<option> Lesotho");
        country.append(
            "<option> Liberia<option> Liechtenstein<option> Lithuania<option> Luxembourg<option> Macau<option> Macedonia");
        country.append(
            "<option> Madagascar<option> Malawi<option> Malaysia<option> Maldives<option> Mali<option> Malta<option> Marshall Islands");
        country.append(
            "<option> Martinique<option> Mauritania<option> Mauritius<option> Mayotte<option> Mexico");
        country.append(
            "<option> Micronesia, Federated States of<option> Moldova, Republic of<option> Monaco<option> Mongolia");
        country.append(
            "<option> Montserrat<option> Morocco<option> Mozambique<option> Myanmar<option> Namibia<option> Nauru<option> Nepal");
        country.append(
            "<option> Netherlands<option> Netherlands Antilles<option> New Caledonia<option> New Zealand<option> Nicaragua");
        country.append(
            "<option> Niger<option> Nigeria<option> Niue<option> Norfolk Island<option> Northern Ireland");
        country.append(
            "<option> Northern Mariana Islands<option> Norway<option> Oman<option> Pakistan<option> Palau<option> Panama");
        country.append(
            "<option> Papua New Guinea<option> Paraguay<option> Peru<option> Philippines<option> Pitcairn<option> Poland");
        country.append(
            "<option> Portugal<option> Puerto Rico<option> Qatar<option> Reunion<option> Romania<option> Russia");
        country.append(
            "<option> Russian Federation<option> Rwanda<option> Saint Kitts and Nevis<option> Saint Lucia");
        country.append(
            "<option> Saint Vincent and the Grenadines<option> Samoa (Independent)<option> San Marino<option> Sao Tome and Principe");
        country.append(
            "<option> Saudi Arabia<option> Scotland<option> Senegal<option> Seychelles<option> Sierra Leone<option> Singapore");
        country.append(
            "<option> Slovakia<option> Slovenia<option> Solomon Islands<option> Somalia<option> South Africa");
        country.append(
            "<option> South Georgia/South Sandwich Islands<option> South Korea<option> Spain<option> Sri Lanka");
        country.append(
            "<option> St. Helena<option> St. Pierre and Miquelon<option> Suriname<option> Svalbard and Jan Mayen Islands");
        country.append(
            "<option> Swaziland<option> Sweden<option> Switzerland<option> Taiwan<option> Tajikistan<option> Tanzania");
        country.append(
            "<option> Thailand<option> Togo<option> Tokelau<option> Tonga<option> Trinidad<option> Trinidad and Tobago");
        country.append(
            "<option> Tunisia<option> Turkey<option> Turkmenistan<option> Turks and Caicos Islands<option> Tuvalu<option> Uganda");
        country.append(
            "<option> Ukraine<option> United Arab Emirates<option> United Kingdom<option selected> United States");
        country.append(
            "<option> United States Minor Outlying Islands<option> Uruguay<option> USA<option> Uzbekistan");
        country.append(
            "<option> Vanuatu<option> Vatican City State (Holy See)<option> Venezuela<option> Viet Nam");
        country.append(
            "<option> Virgin Islands (British)<option> Virgin Islands (U.S.)<option> Wales<option> Wallis and Futuna Islands");
        country.append(
            "<option> Western Sahara<option> Yemen<option> Zambia<option> Zimbabwe");
        country.append("</select>");

        return country.toString();
    }

    /**
     * Creates a SELECT element for US States, the selectedState will be
     * defaulted in the drop down.
     */

    public static String[][] getStateArray() {

        String[][]   states    = {
            { "AK", "Alaska" }, { "AL", "Alabama" }, { "AR", "Arkansas" },
            { "AZ", "Arizona" }, { "CA", "California" }, { "CO", "Colorado" },
            { "CT", "Connecticut" }, { "DC", "District of Columbia" },
            { "DE", "Delaware" }, { "FL", "Florida" }, { "GA", "Georgia" },
            { "HI", "Hawaii" }, { "IA", "Iowa" }, { "ID", "Idaho" },
            { "IL", "Illinois" }, { "IN", "Indiana" }, { "KS", "Kansas" },
            { "KY", "Kentucky" }, { "LA", "Louisiana" },
            { "MA", "Massachusetts" }, { "MD", "Maryland" },
            { "ME", "Maine" }, { "MI", "Michigan" }, { "MN", "Minnesota" },
            { "MO", "Missouri" }, { "MS", "Mississippi" },
            { "MT", "Montana" }, { "NC", "North Carolina" },
            { "ND", "North Dakota" }, { "NE", "Nebraska" },
            { "NH", "New Hampshire" }, { "NJ", "New Jersey" },
            { "NM", "New Mexico" }, { "NV", "Nevada" }, { "NY", "New York" },
            { "OH", "Ohio" }, { "OK", "Oklahoma" }, { "OR", "Oregon" },
            { "PA", "Pennsylvania" }, { "RI", "Rhode Island" },
            { "SC", "South Carolina" }, { "SD", "South Dakota" },
            { "TN", "Tennessee" }, { "TX", "Texas" }, { "UT", "Utah" },
            { "VA", "Virginia" }, { "VT", "Vermont" }, { "WA", "Washington" },
            { "WI", "Wisconsin" }, { "WV", "West Virginia" },
            { "WY", "Wyoming" }
        };

        return states;
    }

    public static String[][] getTimeZonesArray() {

        String[][]   tz    = {
            { "Central", "Central" }, { "Pacific", "Pacific" }, { "Mountain", "Mountain" },
            { "Eastern", "Eastern" },
            { "Eastern +1", "Eastern +1" },
            { "Eastern +2", "Eastern +2" },
            { "Eastern +3", "Eastern +3" },
            { "Eastern +4", "Eastern +4" },
            { "Eastern +5", "Eastern +5" },
            { "Eastern +6", "Eastern +6" },
            { "Pacific -1", "Pacific -1" },
            { "Pacific -2", "Pacific -2" },
            { "Pacific -3", "Pacific -3" },
            { "Pacific -4", "Pacific -4" },
            { "Pacific -5", "Pacific -5" },
            { "Pacific -6", "Pacific -6" },
            { "Pacific -7", "Pacific -7" }

        };

        return tz;
    }

    public static String getStateMenu(String selectName,
                                      String selectedState) {

        String[][]   states    = {
            { "AK", "Alaska" }, { "AL", "Alabama" }, { "AR", "Arkansas" },
            { "AZ", "Arizona" }, { "CA", "California" }, { "CO", "Colorado" },
            { "CT", "Connecticut" }, { "DC", "District of Columbia" },
            { "DE", "Delaware" }, { "FL", "Florida" }, { "GA", "Georgia" },
            { "HI", "Hawaii" }, { "IA", "Iowa" }, { "ID", "Idaho" },
            { "IL", "Illinois" }, { "IN", "Indiana" }, { "KS", "Kansas" },
            { "KY", "Kentucky" }, { "LA", "Louisiana" },
            { "MA", "Massachusetts" }, { "MD", "Maryland" },
            { "ME", "Maine" }, { "MI", "Michigan" }, { "MN", "Minnesota" },
            { "MO", "Missouri" }, { "MS", "Mississippi" },
            { "MT", "Montana" }, { "NC", "North Carolina" },
            { "ND", "North Dakota" }, { "NE", "Nebraska" },
            { "NH", "New Hampshire" }, { "NJ", "New Jersey" },
            { "NM", "New Mexico" }, { "NV", "Nevada" }, { "NY", "New York" },
            { "OH", "Ohio" }, { "OK", "Oklahoma" }, { "OR", "Oregon" },
            { "PA", "Pennsylvania" }, { "RI", "Rhode Island" },
            { "SC", "South Carolina" }, { "SD", "South Dakota" },
            { "TN", "Tennessee" }, { "TX", "Texas" }, { "UT", "Utah" },
            { "VA", "Virginia" }, { "VT", "Vermont" }, { "WA", "Washington" },
            { "WI", "Wisconsin" }, { "WV", "West Virginia" },
            { "WY", "Wyoming" }
        };
        StringBuffer stateList = new StringBuffer(2000);
        StringBuffer stateMenu = new StringBuffer(2000);

        stateMenu.append("<select name=\"" + selectName + "\">");

        int i;

        for (i = 0; i < states.length; i++) {
            stateList.append("<option ");

            // check for selected match
            if (states[i][0].equalsIgnoreCase(selectedState)) {
                stateList.append("selected ");
            }

            stateList.append("value=\"" + states[i][0] + "\">"
                             + states[i][1]);
        }

        stateMenu.append(stateList.toString() + "</select>");

        return stateMenu.toString();
    }

    public static String getStatusMenu(String selectName, String status) {

        StringBuffer menu = new StringBuffer(255);

        menu.append("<select name=\"" + selectName + "\">");

        if (status.equalsIgnoreCase("i")) {
            menu.append("<option value=\"A\">Active");
            menu.append("<option selected value=\"I\">Inactive");
        } else {
            menu.append("<option selected value=\"A\">Active");
            menu.append("<option value=\"I\">Inactive");
        }

        menu.append("</select>");

        return menu.toString();
    }

    public static String getStatusStr(String status) {

        if (status.equalsIgnoreCase("a")) {
            return "Active";
        } else if (status.equalsIgnoreCase("i")) {
            return "Inactive";
        } else {
            return "";
        }
    }

    public static String formatTime(String secondStr) {

        String time    = "";
        long   seconds = 0;
        long   minutes = 0;
        long   hours   = 0;

        try {
            seconds = Long.parseLong(secondStr);

            // format into its parts
            if (seconds > 59) {

                // divide out our seconds into minutes
                minutes = seconds / 60;
                seconds = seconds % 60;

                if (minutes > 59) {
                    hours   = minutes / 60;
                    minutes = minutes % 60;
                }
            }

            // put it all together into a formatted string
            DecimalFormat mask = new DecimalFormat("00");

            time = mask.format(hours) + ":" + mask.format(minutes) + ":"
                   + mask.format(seconds);
        } catch (NumberFormatException e) {
            time = "00:00:00";
        }

        return time;
    }

    public static String getInput(String name, int size, int maxLength, String type,
                                  String existingValue, boolean readOnly, String anchor,
                                  String textClass) throws Exception {

        return getInput(name,size,maxLength,type,existingValue,readOnly,anchor,textClass,null);
    }


    public static String getInput(String name, int size, int maxLength, String type,
                                  String existingValue, boolean readOnly, String anchor,
                                  String textClass, String onChange) throws Exception {
                                

        StringBuffer sb = new StringBuffer();
        
        String roValue = null;
        String rwValue = null;
        if ( existingValue.equals(" ") || 
             existingValue == null || 
             existingValue.length() == 0 || 
             existingValue.equalsIgnoreCase("null") ) {
         roValue = "&nbsp;";
         rwValue = "";
       } else {
         roValue = existingValue;
         rwValue = existingValue;
       }

        if ( readOnly ) {
            sb.append(roValue);
            sb.append("<input type=\"hidden\" " +
                      "name=\""       + name +          "\" " +
                      "value=\""      + existingValue + "\">");
        } else {
            sb.append("<input type=\""       + type +          "\" " +
                             "name=\""       + name +          "\" " +
                             "value=\""      + rwValue +      "\" " +
                             "size=\""       + size +          "\" " +
                             "maxlength=\""  + maxLength +     "\" " +
                             "class=\""      + textClass +     "\" " +
                             (onChange!=null?"onChange=\""+ onChange + "\" ":"") +
                             (existingValue.startsWith("Enter ")?"onfocus=\"this.value='';\"":"") +
                             ">");
            if ( anchor != null ) {
                sb.append(anchor);
            }
        }
        Tracer.log("Got Input HTML for " + name, Tracer.DEBUG, "");
        return sb.toString();
    }

    public static String getCheckbox(String name, String checkedValue,
                                     String existingValue, boolean readOnly,
                                     String anchor, String onChange, boolean disabled) throws Exception {

        StringBuffer sb = new StringBuffer();
        String disabledstr = " ";
        if ( disabled ) { 
          disabledstr = "DISABLED";
        }
          
        sb.append("<input type=\"checkbox\"" +
                         " name=\""       + name         + "\"" +
                         " style=\"height: 15px; width: 15px;\"" +
                         " value=\""      + checkedValue + "\"" +
                         (checkedValue.equals(existingValue)?" CHECKED":"") +
                         " " + disabledstr + ">");

        return sb.toString();
    }

    public static String getHidden(String name, String value) {
        return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\">\n";
    }
    
    public static String getSelect(String name, ArrayList attributeList,
        String selectedValue, int maxWidth, 
        String instructionPhrase, String onChange, 
        String textClass, boolean readOnly) throws Exception {
    	return getSelect(name, attributeList, selectedValue, maxWidth, instructionPhrase, onChange,
    			             textClass, readOnly, " ");
    }

    public static String getSelect(String name, ArrayList attributeList,
                                   String selectedValue, int maxWidth, 
                                   String instructionPhrase, String onChange, 
                                   String textClass, boolean readOnly,
                                   String prePend) throws Exception {
                                   
      Tracer.log("Building Select Html for variable " + name + ", size of list is " + attributeList.size(), Tracer.DEBUG, OBJECT_NAME);
      Tracer.log("Selected Value is " + selectedValue, Tracer.DEBUG, OBJECT_NAME);

      String sv = null;
      if ( selectedValue == null || selectedValue.equals("") ) {
        sv = "-1";
      } else {
        sv = selectedValue;
      }

      StringBuffer sb = new StringBuffer();
      try {

        String display = null;

        if ( readOnly ) {
            display = extractValue(sv, attributeList);
            sb.append("<input type=\"hidden\" " +
                             "name=\""       + name +          "\" " +
                             "value=\""      + sv + "\">\n");
            sb.append("<table border=0 cellspacing=0 cellpadding=0><tr><td valign='center' class=\"" + textClass + "\">");
            sb.append((display==null)?"":display);
            sb.append("</td></tr></table>");
        } else {

            sb.append("<font size='-2'><select name=\"" + name + "\" class=\"" + textClass + "\" ");
            if (onChange != null ) {
                sb.append("onChange=\"" + onChange + "\" ");
            }
            sb.append(" size=1 class=\"" + textClass + "\">\n");

            if (instructionPhrase != null  ) {
                sb.append("<option class=\"" + textClass + "\" value=\"-1\" selected>" + instructionPhrase + "&nbsp;&nbsp;&nbsp;</option>\n");
            }

            for ( int i=0; i < attributeList.size(); i++ ) {

                SelectEntry se = (SelectEntry) attributeList.get(i);

                sb.append("<option class=\"" + textClass + "\" value=\"");
                sb.append(se.getValue());
                sb.append("\"");
                sb.append( ( se.getValue().equals(sv) ) ? " selected" : " " );
                sb.append(">");

                // truncate the displayed value to maxWidth characters
                if ( se.getValue() != null ) {
                  sb.append( (String) prePend + se.getDisplay() );
                }
                sb.append("</option>\n");
            }
            sb.append("</select></font>\n");
        }
      } catch ( Exception e ) {
        Tracer.log(e, "Error creating html for select for field " + name + " size of list is " + attributeList.size(), 
                   Tracer.ERROR,  "HtmlComponent");
        throw e;
      }

      return sb.toString();
    }

    public static String getMultipleSelect(String name, ArrayList attributeList,  // ArrayList of SelectEntry Objects
                                           ArrayList selectedValues, int maxWidth, String instructionPhrase,
                                           String textClass, boolean readOnly, int length) throws Exception {

        if ( attributeList == null ) {
            throw new Exception("attributeList parameter is null");
        }

        StringBuffer sb = new StringBuffer();

        try {

            if ( readOnly ) {

                ArrayList display = new ArrayList();
                display = extractValues(selectedValues, attributeList);
                // xxx - need to figure out how to handle loading up the request with these hiddens at some point.
                //       its not an issue right now because i won't be relying on storing multiple select read only
                //       values on the form...
                //sb.append("<input type=\"hidden\" " +
                //                 "name=\""       + name +          "\" " +
                //                 "value=\""      + ( display == null ? " ":display) + "\">\n");
                sb.append("<div class=\"" + textClass + "\">");
                for ( int i=0; i<display.size(); i++ ) {
                    String st = (String) display.get(i);
                    sb.append(st + ", ");
                }
                if ( sb.length() > 2 ) {
                    sb.setLength(sb.length()-2);
                }
                sb.append("</div>");

            } else {

                sb.append("<font size='-1'><select multiple name=\"" + name + "\" size=3 class=\"" + textClass + "\">\n");

                if (instructionPhrase != null) {
                    attributeList.add(0, new SelectEntry("-1",instructionPhrase));
                }

                if ( selectedValues.size() == 0 ) {
                    selectedValues.add(new String("-1"));
                }

                Tracer.log("selectedValues ArrayList", Tracer.METHOD, "");
                for ( int i=0; i<selectedValues.size(); i++ ) {
                    Tracer.log( (String) selectedValues.get(i), Tracer.METHOD, "");
                }

                for ( int i=0; i < attributeList.size(); i++ ) {

                    SelectEntry se = (SelectEntry) attributeList.get(i);

                    sb.append("<option class=\"" + textClass + "\" value=\"");
                    sb.append(se.getValue());
                    sb.append("\"");
                    Tracer.log("Current Value : " + se.getValue(),Tracer.METHOD,"");
                    Tracer.log("Instruction Phrase : " + instructionPhrase, Tracer.METHOD, "");




                    if ( selectedValues.contains(se.getValue() ) ) {
                        sb.append(" selected");
                    }
                    sb.append(">\n");

                    // truncate the displayed value to maxWidth characters
                    if ( se.getValue() != null ) {
                        if ( ((String) se.getDisplay()).length() > maxWidth ) {
                            sb.append( ((String) se.getDisplay()).substring(0,maxWidth));
                        } else {
                            sb.append( (String) se.getDisplay() );
                        }
                    }
                    sb.append("</option>\n");
                }
                sb.append("</select></font>\n");
            }
        } catch ( Exception e ) {
            Tracer.log(e,"Error building Multiple Select",Tracer.ERROR,"");
            throw e;
        }

        return sb.toString();
    }

    public static String getTimeHtml(String formName, String formElementName, String style, Timestamp date) throws Exception {
    
        Tracer.log("incoming value: " + date.toString(), Tracer.DEBUG, "HtmlComponent.getTimeHtml");

        StringBuffer sb = new StringBuffer();

        sb.append("<table cellpadding=0 cellspacing=0><tr>");
        sb.append("<td>");
        sb.append(HtmlComponent.getSelect(formElementName+"hr",
                                          HtmlComponent.getHours(),
                                          DateTranslator.getHours(date),
                                          999,null,null,style,false));
                                          
        sb.append("<b>:</b>");
                                          
        sb.append(HtmlComponent.getSelect(formElementName+"min",
                                          HtmlComponent.getMinutes(5),
                                          DateTranslator.getMinutes(date),
                                          999,null,null,style, false));
        sb.append("</td>");
        sb.append("</tr></table>");
        
        return sb.toString();
    }

    public static String getDateHtml(String formName, String formElementName, String style, long value) throws Exception {
        // Used where we know the long value of the date. Returns the form element...
//        return HtmlComponent.getInput(formElementName, 13, 13, "text", 
//                                      DateTranslator.getStringDate(new Date(value), DateTranslator.HISTORY_DATEONLY), false,
//                                      HtmlComponent.getCalendar1Anchor(formName,formElementName), style);
        return HtmlComponent.getInput(formElementName, 15, 15, "text", 
                                      DateTranslator.getStringDate(new Date(value), DateTranslator.HISTORY_DATEONLY), false,
                                      null, style);
    }

    public static String getDateHtml(String formName, String formElementName, String style, String value) throws Exception {
        // Used where we know the String value of the date. Returns the form element...
        return HtmlComponent.getInput(formElementName, 15, 15,"text", value ,false,
                                      HtmlComponent.getCalendar1Anchor(formName,formElementName), style);
    }

    public static String getCalendar1Anchor(String formName, String formElementName) throws Exception {

        return "<A href=\"javascript:doNothing()\" onClick=\"setDateField(document." + formName +
               "." + formElementName + ");\n " +
               "top.newWin = window.open('../report/calendar.jsp','cal','dependent=yes,width=210,height=230," +
               " screenX=200,screenY=300,titlebar=yes')\"><IMG SRC=\"../images/calendar.gif\" BORDER=0" +
               " alt=\"Popup Calendar\"></A>\n";
    }

    public static String getCalendar1JsRef() {
        return "<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"../jscript/calendar.js\"></SCRIPT>";
    }

    public static String getDynamicListJsRef() {
        return "<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"../jscript/DynamicOptionList.js\"></SCRIPT>";
    }


    private static String extractValue(String key, ArrayList selectList) {

        String displayValue = null;

        for ( int i=0; i<selectList.size(); i++) {
            SelectEntry se = (SelectEntry) selectList.get(i);
            if ( se.getValue().equals(key) ) {
                displayValue = se.getDisplay();
                break;
            }
        }

        return displayValue;
    }

    private static ArrayList extractValues(ArrayList keys, ArrayList selectList) {

        ArrayList displays = new ArrayList();

        for ( int i=0; i<keys.size(); i++ ) {
            String key = (String) keys.get(i);
            for ( int j=0; j<selectList.size(); j++) {
                SelectEntry se = (SelectEntry) selectList.get(j);
                if ( se.getValue().equals(key) ) {
                    displays.add(se.getDisplay());
                    break;
                }
            }
        }
        return displays;

    }

    public static ArrayList getAm() {

        ArrayList am = new ArrayList(2);

        am.add(new SelectEntry("AM","AM"));
        am.add(new SelectEntry("PM","PM"));

        return am;
    }

    public static ArrayList getMinutes(int interval) {

      ArrayList mins = new ArrayList(12);
      
      for ( int i=0; i<=59; i++ ) {
      
        if ( i%interval == 0 ) {
          String value = Integer.toString(i);          
          if ( value.length() == 1 ) value = "0" + value;
          mins.add(new SelectEntry(value, value) );
        }
      }

      return mins;

    }

    public static ArrayList getHours() {

        ArrayList hrs = new ArrayList(24);
        hrs.add(new SelectEntry("00","00"));
        hrs.add(new SelectEntry("01","01"));
        hrs.add(new SelectEntry("02","02"));
        hrs.add(new SelectEntry("03","03"));
        hrs.add(new SelectEntry("04","04"));
        hrs.add(new SelectEntry("05","05"));
        hrs.add(new SelectEntry("06","06"));
        hrs.add(new SelectEntry("07","07"));
        hrs.add(new SelectEntry("08","08"));
        hrs.add(new SelectEntry("09","09"));
        hrs.add(new SelectEntry("10","10"));
        hrs.add(new SelectEntry("11","11"));
        hrs.add(new SelectEntry("12","12"));
        hrs.add(new SelectEntry("13","13"));
        hrs.add(new SelectEntry("14","14"));
        hrs.add(new SelectEntry("15","15"));
        hrs.add(new SelectEntry("16","16"));
        hrs.add(new SelectEntry("17","17"));
        hrs.add(new SelectEntry("18","18"));
        hrs.add(new SelectEntry("19","19"));
        hrs.add(new SelectEntry("20","20"));
        hrs.add(new SelectEntry("21","21"));
        hrs.add(new SelectEntry("22","22"));
        hrs.add(new SelectEntry("23","23"));

        return hrs;
    }


}
