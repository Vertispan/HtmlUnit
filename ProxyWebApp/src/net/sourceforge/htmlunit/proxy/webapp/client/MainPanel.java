package net.sourceforge.htmlunit.proxy.webapp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class MainPanel extends Composite { 

    interface Binder extends UiBinder<Widget, MainPanel> { } 
    private static final Binder binder = GWT.create(Binder.class); 

    @UiField Button clearButton;
    @UiField TextArea logTextArea;

    public MainPanel() {
        initWidget(binder.createAndBindUi(this));
        clearButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(final ClickEvent event) {
                logTextArea.setText("");
            }
        });
    } 
} 
