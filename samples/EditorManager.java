/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Oct 23, 2002
 * Time: 5:30:41 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package samples;

import com.powerdba.mvc.View;
import com.powerdba.mvc.PresentationEnvironment;
import com.powerdba.mvc.jsp.*;
import com.powerdba.mvc.WsnException;
import com.powerdba.util.Tracer;
import com.powerdba.jdbc.ConnectionManager;
import com.powerdba.jdbc.DbConfigDAO;
import com.powerdba.jdbc.DbConfig;

import java.util.ArrayList;
import java.util.Vector;
import java.sql.SQLException;
import java.sql.Connection;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class EditorManager extends JspController {

    private EditorView myView;
    private Connection conn;
    private String key = " ";
    private String database;

    //Constructors
    public EditorManager(PresentationEnvironment env) {
        super(env);
        // build the view
        myView = new EditorView(env);
    }

    // Main Process
    public View process() throws Exception {
        // load incoming variables for page navigation

        try {
            // Get the specified action from the environment...
            action = env.getInt("action");
            key    = env.getParameter("key");
            database = env.getParameter("database");
            myView.setDatabase(database);
            if ( database != "" ) { setConnection(database); }
            
            // Get the current database from the env.
            if ( key == "") { action = NEW; }

            switch (action) {
                case EDIT_RULE:
                    myView.setStep("Edit");
                    myView.setObjectToEdit(EditorDAO.getRuleObject(conn, key));
                    break;
                case EDIT_RULE_SUBMIT:
                    EditorDAO.updateRuleObject(conn, env.getParameter("ruletext"));
                    myView.setStep("RefreshParent");
                    break;
            }

            myView.build();

        } catch ( WsnException w ) {
            throw new WsnException("EditorManager", w.getMessage());
        } catch ( Exception e ) {
            Tracer.log(e, "Error in the PowerDBA EditorManager process() method " + e.getMessage(), Tracer.ERROR, this);
            try {
                if ( conn != null ) this.conn.rollback();
            } catch ( Exception e2 ) {
                Tracer.log(e2, "Error rolling back the database trx", Tracer.ERROR, this);
                throw e2;
            }
            throw new WsnException("PowerDBA Editor Manager - Processing editor",e.getMessage());

        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( Exception e3 ) {
                    Tracer.log(e3,"Error closing connection to the database", Tracer.WARNING, this);
                }
            }
        }
        return myView;
    }

    private void setConnection(String db) throws Exception {

        try {
            if ( this.conn != null ) {
                try {
                    conn.close();
                    Tracer.log("Closed the current connection (actually only releases it back to the pool)...", Tracer.DEBUG, this);
                } catch ( Exception e3 ) {
                    Tracer.log(e3,"Error closing connection to the database", Tracer.WARNING, this);
                }
            }

            Tracer.log("Opening a new Connection to " + db, Tracer.DEBUG, this);
            this.conn = ConnectionManager.getConnection(db);
            //this.database = db;
            this.conn.setAutoCommit(false);
        } catch ( Exception e ) {
            Tracer.log("Error getting a connection to the database", Tracer.ERROR, this);
            throw e;
        }
    }



}

