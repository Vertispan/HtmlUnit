package net.sourceforge.htmlunit.proxy;

import java.io.IOException;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * HtmlUnit Filter, initial version.
 *
 * @author Ahmed Ashour
 */
public class HtmlUnitFilter implements Filter {

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] filter(Request arg0, MimeHeaders arg1, byte[] arg2) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldFilter(Request arg0, MimeHeaders arg1) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean init(Server arg0, String arg1) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean respond(Request arg0) throws IOException {
        return false;
    }

}
