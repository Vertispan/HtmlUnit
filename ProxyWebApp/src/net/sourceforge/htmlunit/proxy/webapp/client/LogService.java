package net.sourceforge.htmlunit.proxy.webapp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("log")
public interface LogService extends RemoteService {

    /**
     * Returns the logs.
     * @param index the starting index
     * @return the logs
     */
    public String[] getLog(final int index);

}
