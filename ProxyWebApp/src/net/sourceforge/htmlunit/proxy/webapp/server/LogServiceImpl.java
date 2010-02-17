package net.sourceforge.htmlunit.proxy.webapp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.htmlunit.proxy.webapp.client.LogService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
public class LogServiceImpl extends RemoteServiceServlet implements LogService {

    private static final long serialVersionUID = 3951561955277264689L;

    private final static List<String> logs_ = new ArrayList<String>();

    static {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                synchronized (logs_) {
                    logs_.add("Log #" + (logs_.size() + 1));
                }
            }
        }, 0, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getLog(int index) {
        while(true) {
            synchronized(logs_) {
                if (index < logs_.size()) {
                    final String[] list = new String[logs_.size() - index];
                    for (int i = 0; i < list.length; i++) {
                        list[i] = logs_.get(index + i);
                    }
                    return list;
                }
            }
            try {
                Thread.sleep(100);
            }
            catch(final Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
