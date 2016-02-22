/*
 * Copyright (c) 2002-2016 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.javascript.host.event;

import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.IE;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClasses;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstant;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.WebBrowser;

/**
 * JavaScript object representing a UI event. For general information on which properties and functions should be
 * supported, see <a href="http://www.w3.org/TR/DOM-Level-3-Events/events.html#Events-UIEvent">DOM Level 3 Events</a>.
 *
 * @author Daniel Gredler
 * @author Ahmed Ashour
 * @author Frank Danek
 */
@JsxClasses({
        @JsxClass(browsers = { @WebBrowser(CHROME), @WebBrowser(FF), @WebBrowser(IE),
                @WebBrowser(EDGE) })
    })
public class UIEvent extends Event {

    /** Constant. */
    @JsxConstant(@WebBrowser(FF))
    public static final int SCROLL_PAGE_DOWN = 0x8000;

    /** Constant. */
    @JsxConstant(@WebBrowser(FF))
    public static final short SCROLL_PAGE_UP = 0xFFFF8000;

    /** Specifies some detail information about the event. */
    private long detail_;

    /** Whether or not the "meta" key was pressed during the firing of the event. */
    private boolean metaKey_;

    /**
     * Creates a new UI event instance.
     */
    @JsxConstructor({ @WebBrowser(CHROME), @WebBrowser(FF), @WebBrowser(EDGE) })
    public UIEvent() {
    }

    /**
     * Creates a new UI event instance.
     *
     * @param domNode the DOM node that triggered the event
     * @param type the event type
     */
    public UIEvent(final DomNode domNode, final String type) {
        super(domNode, type);
    }

    /**
     * Creates a new event instance.
     * @param scriptable the SimpleScriptable that triggered the event
     * @param type the event type
     */
    public UIEvent(final SimpleScriptable scriptable, final String type) {
        super(scriptable, type);
    }

    /**
     * Returns some detail information about the event, depending on the event type. For mouse events,
     * the detail property indicates how many times the mouse has been clicked in the same location for
     * this event.
     *
     * @return some detail information about the event, depending on the event type
     */
    @JsxGetter({ @WebBrowser(CHROME), @WebBrowser(FF), @WebBrowser(IE) })
    public long getDetail() {
        return detail_;
    }

    /**
     * Sets the detail information for this event.
     *
     * @param detail the detail information for this event
     */
    protected void setDetail(final long detail) {
        detail_ = detail;
    }

    /**
     * @return indicates if event propagation is stopped
     */
    @Override
    @JsxGetter
    public boolean getCancelBubble() {
        return super.getCancelBubble();
    }

    /**
     * @param newValue indicates if event propagation is stopped
     */
    @Override
    @JsxSetter
    public void setCancelBubble(final boolean newValue) {
        super.setCancelBubble(newValue);
    }

    /**
     * Returns the view from which the event was generated. In browsers, this is the originating window.
     *
     * @return the view from which the event was generated
     */
    @JsxGetter({ @WebBrowser(CHROME), @WebBrowser(FF), @WebBrowser(IE) })
    public Object getView() {
        return getWindow();
    }

    /**
     * Implementation of the DOM Level 3 Event method for initializing the UI event.
     *
     * @param type the event type
     * @param bubbles can the event bubble
     * @param cancelable can the event be canceled
     * @param view the view to use for this event
     * @param detail the detail to set for the event
     */
    @JsxFunction({ @WebBrowser(CHROME),  @WebBrowser(FF), @WebBrowser(IE) })
    public void initUIEvent(
            final String type,
            final boolean bubbles,
            final boolean cancelable,
            final Object view,
            final int detail) {
        initEvent(type, bubbles, cancelable);
        // Ignore the view parameter; we always use the window.
        setDetail(detail);
    }

    /**
     * Returns whether or not the "meta" key was pressed during the event firing.
     * @return whether or not the "meta" key was pressed during the event firing
     */
    @JsxGetter({ @WebBrowser(FF), @WebBrowser(CHROME), @WebBrowser(IE) })
    public boolean getMetaKey() {
        return metaKey_;
    }

    /**
     * @param metaKey whether Meta has been pressed during this event or not
     */
    protected void setMetaKey(final boolean metaKey) {
        metaKey_ = metaKey;
    }

}
