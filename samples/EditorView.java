/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Oct 23, 2002
 * Time: 5:35:28 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package samples;

import com.powerdba.SourceObject;
import java.text.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.powerdba.mvc.*;
import com.powerdba.mvc.jsp.*;
import com.powerdba.util.*;

/**
 * This is the base class to handle the PowerDBA Display Layer
 *
 * @author kguisti
 */
public class EditorView extends JspView {

    public static final  String FORM = "pdbaedit";
    
    private static final String DETAILFONT = "verysmallentry";
    private static final String FONT = "smallentry";
    private static final boolean debug = true;

    private int currAction;
    private SourceObject objectToEdit;
    private String database;
    private String editHtml;

    private StringBuffer onLoadActions = new StringBuffer();

    private String       jsp = "editor.jsp";
    private Connection   conn;

    //Constructors
    public EditorView(PresentationEnvironment env) {
        super(env);
    }

    // will run page specific methods that will build each dynamic portion of the page
    public void build() throws Exception {
        Tracer.log("Building Editor View", Tracer.DEBUG,this);

        try {
            buildFormOpen();
            buildJavaScript();
            buildEditHtml();
        } catch ( Exception e ) {
            Tracer.log(e,"Error occurred somewhere in the build() method " + e.getMessage(), Tracer.ERROR, this);
            throw e;
        }
    }

    public void setObjectToEdit(SourceObject source) { this.objectToEdit = source; }

    public String getEditHtml() { return editHtml; }

    public void setDatabase(String db) {this.database = db; }

    // build a form open and form hiddens
    public void buildFormOpen() {
        // Add form and hiddens
        formOpen.append("<form method=\"post\" action=\"" + jsp + "\" name=\"" + FORM + "\">\n");
        formOpen.append("<input type=\"hidden\" name=\"formaction\" value=\""+ currAction + "\">\n");
        formOpen.append("<input type=\"hidden\" name=\"action\" value=\""+ PowerDbaActions.EDIT_RULE_SUBMIT + "\">\n");
        formOpen.append("<input type=\"hidden\" name=\"target\" value=\""+ 0 + "\">\n");
        formOpen.append("<input type=\"hidden\" name=\"source\" value=\""+ 0 + "\">\n");
        formOpen.append("<input type=\"hidden\" name=\"mode\" value=\"" + 0 + "\">\n");
        formOpen.append("<input type=\"hidden\" name=\"database\" value=\"" + this.database + "\">\n");
    }

    // Private Methods...
    private String buildSelectBox(int i) {
        return "<td><input name=select"+i+" type=checkbox value=\"on\"></td>";
    }

    private void buildEditHtml() {
       this.editHtml = HtmlComponent.getTextArea("ruletext",objectToEdit.getText(),80,10);
    }

    private void buildJavaScript() {

        // Generate Javascript
        javaScript.append("<script>                                                           \n");
        javaScript.append("   var win;                                                        \n");
        javaScript.append("   function explain(key) {                                          \n");
        javaScript.append("     if ( win ) {                                                  \n");
        javaScript.append("        win.close();                                               \n");
        javaScript.append("     }                                                             \n");
        javaScript.append("     win = window.open('editor.jsp?action=" + WsnIndex.EDIT_RULE +
                          "&key='+key+'&database="+database+"','editPopup','width=600,height=450,scrollbars=yes');\n");
        javaScript.append("     win.focus();                                                  \n");
        javaScript.append("   }                                                               \n");
        javaScript.append("   function submitChange(myForm, , dbaction) {                            \n");
        javaScript.append("        myForm.target.value     = dbaction;                         \n");
        javaScript.append("        myForm.formaction.value = dbaction                          \n");
        javaScript.append("        myForm.source.value     = \"" + 0 + "\";                    \n");
        javaScript.append("        myForm.mode.value       = \"" + 0 + "\";                    \n");
        javaScript.append("        myForm.action.value     = \"" + PowerDbaActions.EDIT_RULE_SUBMIT + "\";     \n");
        javaScript.append("        myForm.submit();                                             \n");
        javaScript.append("   }                                                                \n");
        javaScript.append("</script>                                                           \n");
    }

    public String getHiddens() {
        return HtmlComponent.getHidden("database",database);
    }

    public String getOnLoadActions() { return this.onLoadActions.toString(); }

    public String getFormAction(String actionReq) throws Exception {

        Tracer.log("Getting Form Action: " + actionReq, Tracer.DEBUG, "EditorView");

        String action = null;
        String formAction = "";

        // initial null check
        if (actionReq != null) {
            action = actionReq.toLowerCase();
        }

        if (action.equals("cancel")) {
            formAction = HtmlComponent.getSmallFormButton("Cancel", "Cancel Changes",
                                                          "go(" + FORM + ",'" + jsp + "'," + WsnIndex.DB_FREESPACE
                                                          + ")", 75,15);
        } else if (action.equals("submit")) {
            formAction = HtmlComponent.getSmallFormButton("Save", "Save Changes",
                                                          "go(" + FORM + ",'" + jsp + "'," + WsnIndex.EDIT_RULE_SUBMIT
                                                          + ")", 75,15);
        } else if (action.equals("streams")) {
            //formAction = HtmlComponent.getSmallFormButton("Streams", "Oracle Streams",
            //                                              "go(" + FORM + ",'" + jsp + "'," + WsnIndex.DB_STREAMS
            //                                              + ",'" + database + "')", 75,15);
            formAction = HtmlComponent.getSmallButton("Streams","powerdba.jsp?action="+WsnIndex.DB_STREAMS_SUMMARY+"&database="+database,null,75,15);
        } else if (action.equals("deletepartition")) {
            formAction = HtmlComponent.getImageButton("Delete", "../images/delete1.gif", "deletepartition(" + FORM + ",'" + jsp + "')", "Delete this Partition");
        } else if (action.equals("deleteelements")) {
            formAction = HtmlComponent.getFormButton("Delete", "Delete Selected Elements", "deleteelements(" + FORM + ",'" + jsp + "')", 120,15);
        } else if (action.equals("clear")) {
            formAction = HtmlComponent.getFormButton("Create Partition", "Clear the current Partition and create a new one", "createnew(" + FORM + ",'" + jsp + "')", 120,15);
        } else if (action.equals("get")) {
            formAction = HtmlComponent.getSmallFormButton("Go>", "Retrive the Partition", "retrieve(" + FORM + ",'" + jsp + "')", 25,8);
        } else if (action.equals("explain")) {
            formAction = HtmlComponent.getSmallFormButton("Explain Plan>", "Get Explain Plan", "explain(" + FORM + ",'" + jsp + "')", 25,8);
        }
        return formAction;
    }
}

