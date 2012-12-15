package com.powerdba.mvc.jsp;

import com.powerdba.util.*;
import com.powerdba.mvc.*;

public abstract class JspView extends View {

    protected int          target;
    protected int          mode;
    protected int          action;
    protected int          source;
    protected String       formUrl     = "mediaSummary.jsp";
    protected String       header      = "";
    protected String       title       = "";
    protected StringBuffer javaScript  = new StringBuffer(400);
    protected StringBuffer formOpen    = new StringBuffer(250);
    protected StringBuffer resultMsg   = new StringBuffer(250);
    protected StringBuffer formHiddens = new StringBuffer(250);

    // Constructor
    protected JspView(PresentationEnvironment env) {

        super(env);

        buildGlobalJavaScript();
    }

    // abstract classes
    public abstract void build() throws Exception;

    // Get methods
    public String getTitle() throws Exception {
        return title;
    }

    public String getHeader() throws Exception {
        return header;
    }

    public String getResultMsg() throws Exception {
        return resultMsg.toString();
    }

    public String getJavaScript() throws Exception {
        Tracer.log("Getting Javascript",Tracer.DEBUG,"JspView");
        return javaScript.toString();
    }

    public String getFormOpen() throws Exception {
        return formOpen.toString();
    }

    public int getTargetSetting() {
        return target;
    }

    public int getModeSetting() {
        return mode;
    }

    public int getActionSetting() {
        return action;
    }

    public int getSourceSetting() {
        return source;
    }

    public String getFormUrl() {
        return formUrl;
    }

    // set methods
    public void setTargetSetting(int target) {
        this.target = target;
    }

    public void setModeSetting(int mode) {
        this.mode = mode;
    }

    public void setActionSetting(int action) {
        this.action = action;
    }

    public void setSourceSetting(int source) {
        this.source = source;
    }

    public void setFormUrl(String formUrl) {
        this.formUrl = formUrl;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void addToResultMsg(String resultMsg) {
        this.resultMsg.append(resultMsg);
    }

    public void addToFormHiddens(String formHiddens) {
        this.formHiddens.append(formHiddens);
    }

    // Static HtmlComponent calls
    public String buildFormOpen(String url, String name, String target,
                                String action, String source, String mode) {

        String formOpen = HtmlComponent.buildFormOpen(url, name, target,
                              action, source, mode);

        Tracer.log("formOpen = [" + formOpen + "]", Tracer.MINOR, sessionId, this);

        return formOpen;
    }

    public String getImageButton(String name, String image, String onClick, String alt)
            throws Exception {

        String imageButton = HtmlComponent.getImageButton(name, image,
                                 onClick, alt);

        Tracer.log("image button = [" + imageButton + "]", Tracer.MINOR, sessionId,
                   this);

        return imageButton;
    }

    public String getFormButton(
            String name, String windowStatus, String onClick, int width, int height)
                throws Exception {

        String formButton = HtmlComponent.getFormButton(name, windowStatus,
                                onClick, width, height);

        Tracer.log("form button = [" + formButton + "]", Tracer.MINOR, sessionId,
                   this);

        return formButton;
    }

    public String getButton(String name, String href, String jScript, int width, int height)
                throws Exception {

        String sButton = HtmlComponent.getButton(name, href, jScript, width,
                                                 height);

        Tracer.log("link button = [" + sButton + "]", Tracer.MINOR, sessionId, this);

        return sButton;
    }

    public String getButtonNoWrap(String name, String href, String jScript, int width, int height)
                throws Exception {

        String sButton = HtmlComponent.getButton(name, href, jScript, width, height);

        Tracer.log("link button = [" + sButton + "]", Tracer.MINOR, sessionId, this);

        return sButton;
    }

    public String getButtonNoWrapGray(String name, int width, int height)
            throws Exception {

        String sButton = HtmlComponent.getButtonNoWrapGray(name, width,
                             height);

        Tracer.log("link button = [" + sButton + "]", Tracer.MINOR, sessionId, this);

        return sButton;
    }

    public String getButtonGray(String name, int width, int height) throws Exception {

        String sButton = HtmlComponent.getButtonGray(name, width, height);

        Tracer.log("link button = [" + sButton + "]", Tracer.MINOR, sessionId, this);

        return sButton;
    }

    public String getBBTableOpen(int width) {

        String tableOpen = HtmlComponent.getBlackBoxTableOpen(width);

        Tracer.log("table open = [" + tableOpen + "]", Tracer.MINOR, sessionId,
                   this);

        return tableOpen;
    }

    public String getBBTableClose() {

        String tableClose = HtmlComponent.getBlackBoxTableClose();

        Tracer.log("table close= [" + tableClose + "]", Tracer.MINOR, sessionId,
                   this);

        return tableClose;
    }

    public void buildGlobalJavaScript() {

        javaScript.append("<script>\n");
        javaScript.append("    var isNetscape, isIe, is40;\n\n");
        javaScript.append("    if (navigator.appName == \"Netscape\") {\n");
        javaScript.append("        isNetscape = true;\n");
        javaScript.append(
            "    } else if (navigator.appVersion.indexOf(\"MSIE\") != -1) {\n");
        javaScript.append("        isIe = true;\n");
        javaScript.append("    }\n\n");
        javaScript
            .append("    if (navigator.appVersion.charAt(0) == \"4\") {\n");
        javaScript.append(
            "        is40 = isNetscape || isIe;  // sorry Opera people...\n");
        javaScript.append("    }\n");
        javaScript.append("</script>\n\n");
    }

    // utility methods
    protected String buffer(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }
}
