

package com.powerdba.mvc;

import com.powerdba.mvc.jsp.JspEnvironment;

public class LoginView {

    private String loginForm = "login";
    private String title;
    private StringBuffer javaScript = new StringBuffer();
    private String message = " ";

    public LoginView(JspEnvironment env) {}

    // will run page specific methods that will build each dynamic portion of the page
    public void build() {
        // build javascript
        buildJavaScript();
    }

    public void buildJavaScript() {

        // Generate Javascript
        javaScript.append("<script>var errorString = \" \";\n ");

        // validate login function
        javaScript.append("function valLogin() { \n");
        javaScript.append("   if (checkNull(document.login)) { \n");
        javaScript.append("      alert(\"Please use correct login: \" + errorString); \n");
        javaScript.append("      return false; \n");
        javaScript.append("   } \n");
        javaScript.append("   else { \n");
        javaScript.append("      document.login.submit(); \n");
        javaScript.append("   } \n");
        javaScript.append("} \n");

        // checks each entry field
        javaScript.append("function checkNull (myForm) { \n");
        javaScript.append("   if (myForm.adminusername.value == \"\") {errorString = \"Username is null\"; return true;} \n");
        javaScript.append("   if (myForm.adminusername.value.toLowerCase() != \"admin\") {errorString = \"Incorrect Admin Username\"; return true;} \n");
        javaScript.append("   if (myForm.adminpassword.value == \"\") {errorString = \"Password is null\"; return true;} \n");
        javaScript.append("   else {  \n");
        javaScript.append("      errorString = \"\"; \n");
        javaScript.append("      return false; \n");
        javaScript.append("   } \n");
        javaScript.append("} \n\n");
        
        javaScript.append("</script>");
    }
    
    
    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
      
    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    
    /**
     * @return Returns the javaScript.
     */
    public String getJavaScript() {
        return javaScript.toString();
    }
    /**
     * @param javaScript The javaScript to set.
     */
    public void setJavaScript(StringBuffer javaScript) {
        this.javaScript = javaScript;
    }
}

