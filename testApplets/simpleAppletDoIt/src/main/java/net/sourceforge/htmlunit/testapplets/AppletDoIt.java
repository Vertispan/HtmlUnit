package net.sourceforge.htmlunit.testapplets;

import java.applet.Applet;

import netscape.javascript.JSObject;

/**
 * A simple with a single method called "doIt" that echoes the method call in the window's status of the containing page.
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public class AppletDoIt extends Applet {
    private static final long serialVersionUID = -3986100495006620079L;

    public AppletDoIt() {
        super();
    }

    public void doIt(final String message) {
        getAppletContext().showStatus("Called: doIt('" + message + "')");
    }

    public void showCodeBase() {
        getAppletContext().showStatus("CodeBase: '" + getCodeBase().toExternalForm() + "'");
    }

    public void showDocumentBase() {
        getAppletContext().showStatus("DocumentBase: '" + getDocumentBase().toExternalForm() + "'");
    }

    public void showParam(final String paramName) {
        getAppletContext().showStatus(paramName + ": '" + getParameter(paramName) + "'");
    }

    public void execJS(final String javascript) {
        getAppletContext().showStatus("execJS: '" + javascript + "'");
        JSObject window = JSObject.getWindow(this);
        Object result = window.eval(javascript);
        getAppletContext().showStatus("  '" + result + "'");
    }

    public void callWithoutParams(final String methodName) {
        getAppletContext().showStatus("call: '" + methodName + "'");
        JSObject window = JSObject.getWindow(this);
        Object result = window.call(methodName);
        getAppletContext().showStatus("  '" + result + "'");
    }

    public void callWithStringParam(final String methodName) {
        getAppletContext().showStatus("call: '" + methodName + "'");
        JSObject window = JSObject.getWindow(this);
        Object result = window.call(methodName, "HtmlUnit");
        getAppletContext().showStatus("  '" + result + "'");
    }

    public void setValueAttribute(final String id, final String value) {
        JSObject window = JSObject.getWindow(this);
        JSObject input = (JSObject) window.eval("document.getElementById('" + id + "');");
        input.setMember("value", value);
        getAppletContext().showStatus("value set for '" + id + "' to '" + value + "'");
    }
}
