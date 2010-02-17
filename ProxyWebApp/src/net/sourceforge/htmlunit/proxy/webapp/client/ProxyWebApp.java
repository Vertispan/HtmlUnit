package net.sourceforge.htmlunit.proxy.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ProxyWebApp implements EntryPoint {

    private final LogServiceAsync logService = GWT.create(LogService.class);

    private int counter;
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        final MainPanel main = new MainPanel();
        RootLayoutPanel.get().add(main);
        final Timer timer = new Timer() {
            
            @Override
            public void run() {
                logService.getLog(counter, new AsyncCallback<String[]>() {
                    
                    @Override
                    public void onSuccess(final String[] logs) {
                        for (int i = 0; i < logs.length; i++) {
                            main.logTextArea.setText(main.logTextArea.getText() + logs[i] + '\n');
                        }
                        counter += logs.length;
                        main.logTextArea.setCursorPos(main.logTextArea.getText().length());
                    }
                    
                    @Override
                    public void onFailure(final Throwable caught) {
                        Window.alert("Failure connecting to server " + caught);
                    }
                });
            }
        };
        timer.scheduleRepeating(2000);
    }
}
