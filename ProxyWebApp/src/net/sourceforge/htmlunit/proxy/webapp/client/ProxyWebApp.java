/*
 * Copyright (c) 2010 HtmlUnit team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.htmlunit.proxy.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class ProxyWebApp implements EntryPoint {

    private final LogServiceAsync logService_ = GWT.create(LogService.class);

    private int counter_;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        final MainPanel main = new MainPanel();
        RootLayoutPanel.get().add(main);
        final Timer timer = new Timer() {

            @Override
            public void run() {
                logService_.getLog(counter_, new AsyncCallback<String[]>() {

                    @Override
                    public void onSuccess(final String[] logs) {
                        for (int i = 0; i < logs.length; i++) {
                            main.logTextArea_.setText(main.logTextArea_.getText() + logs[i] + '\n');
                        }
                        counter_ += logs.length;
                        main.logTextArea_.setCursorPos(main.logTextArea_.getText().length());
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
