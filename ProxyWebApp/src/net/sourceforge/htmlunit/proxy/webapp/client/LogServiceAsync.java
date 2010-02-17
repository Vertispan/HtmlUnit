package net.sourceforge.htmlunit.proxy.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LogService</code>.
 */
public interface LogServiceAsync {

    /**
     * The async variant of {@link LogService#getLog(int)}}.
     */
     void getLog(int index, AsyncCallback<String[]> callback);
}
