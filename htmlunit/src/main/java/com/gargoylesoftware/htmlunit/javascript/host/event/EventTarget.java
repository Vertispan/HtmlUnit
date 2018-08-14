/*
 * Copyright (c) 2002-2018 Gargoyle Software Inc.
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

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_CALL_RESULT_IS_LAST_RETURN_VALUE;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.JS_EVENT_WINDOW_EXECUTE_IF_DITACHED;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.IE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DomDocumentFragment;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxFunction;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

/**
 * A JavaScript object for {@code EventTarget}.
 *
 * @author Ahmed Ashour
 */
@JsxClass({CHROME, FF, EDGE})
@JsxClass(isJSObject = false, value = IE)
public class EventTarget extends SimpleScriptable {

    private EventListenersContainer eventListenersContainer_;

    /**
     * Default constructor.
     */
    @JsxConstructor
    public EventTarget() {
    }

    /**
     * Allows the registration of event listeners on the event target.
     * @param type the event type to listen for (like "click")
     * @param listener the event listener
     * @param useCapture If {@code true}, indicates that the user wishes to initiate capture
     * @see <a href="https://developer.mozilla.org/en-US/docs/DOM/element.addEventListener">Mozilla documentation</a>
     */
    @JsxFunction
    public void addEventListener(final String type, final Scriptable listener, final boolean useCapture) {
        getEventListenersContainer().addEventListener(type, listener, useCapture);
    }

    /**
     * Gets the container for event listeners.
     * @return the container (newly created if needed)
     */
    public final EventListenersContainer getEventListenersContainer() {
        if (eventListenersContainer_ == null) {
            eventListenersContainer_ = new EventListenersContainer(this);
        }
        return eventListenersContainer_;
    }

    /**
     * Executes the event on this object only (needed for instance for onload on (i)frame tags).
     * @param event the event
     * @return the result
     * @see #fireEvent(Event)
     */
    public ScriptResult executeEventLocally(final Event event) {
        final EventListenersContainer eventListenersContainer = getEventListenersContainer();
        final Window window = getWindow();
        final Object[] args = new Object[] {event};

        final Event previousEvent = window.getCurrentEvent();
        window.setCurrentEvent(event);
        try {
            event.setEventPhase(Event.AT_TARGET);
            return eventListenersContainer.executeAtTargetListeners(event, args);
        }
        finally {
            window.setCurrentEvent(previousEvent); // reset event
        }
    }

    /**
     * Fires the event on the node with capturing and bubbling phase.
     * @param event the event
     * @return the result
     */
    public ScriptResult fireEvent(final Event event) {
        final Window window = getWindow();
        final Object[] args = new Object[] {event};

        event.startFire();
        ScriptResult result = null;
        final Event previousEvent = window.getCurrentEvent();
        window.setCurrentEvent(event);

        // The load event has some unnatural behaviour that we need to handle specially
        final boolean isLoadEvent = Event.TYPE_LOAD.equals(event.getType());

        try {
            // These can be null if we aren't tied to a DOM node
            final DomNode ourNode = getDomNodeOrNull();
            final DomNode ourParentNode = (ourNode != null) ? ourNode.getParentNode() : null;

            boolean isAttached = false;
            for (DomNode node = ourNode; node != null; node = node.getParentNode()) {
                if (node instanceof Document || node instanceof DomDocumentFragment) {
                    isAttached = true;
                    break;
                }
            }

            // Determine the propagation path which is fixed here and not affected by
            // DOM tree modification from intermediate listeners (tested in Chrome)
            final List<EventTarget> propagationPath = new ArrayList<>();

            // The window 'load' event targets Document but paths Window only (tested in Chrome/FF)
            if (!isLoadEvent || !(ourNode instanceof Document)) {
                // We go on the propagation path first
                if (isAttached || !(this instanceof HTMLElement)) {
                    propagationPath.add(this);
                }
                // Then add all our parents if we have any (pure JS object such as XMLHttpRequest
                // and MessagePort, etc. will not have any parents)
                for (DomNode parent = ourParentNode; parent != null; parent = parent.getParentNode()) {
                    final EventTarget jsNode = parent.getScriptableObject();
                    if (isAttached || !(jsNode instanceof HTMLElement)) {
                        propagationPath.add(jsNode);
                    }
                }
            }
            // The 'load' event for other elements target that element and but does not path Window
            // (see Note in https://www.w3.org/TR/DOM-Level-3-Events/#event-type-load)
            if (!isLoadEvent || ourNode instanceof Document) {
                if (isAttached || getBrowserVersion().hasFeature(JS_EVENT_WINDOW_EXECUTE_IF_DITACHED)) {
                    propagationPath.add(window);
                }
            }

            final boolean ie = getBrowserVersion().hasFeature(JS_CALL_RESULT_IS_LAST_RETURN_VALUE);

            // Refactoring note: Not sure of the reasoning for this but preserving nonetheless: Nodes
            // are traversed if they're attached or if they're non-HTMLElement.  However, the capturing
            // phase only traverses nodes that are attached
            if (isAttached) {
                // capturing phase
                event.setEventPhase(Event.CAPTURING_PHASE);

                for (int i = propagationPath.size() - 1; i >= 1; i--) {
                    final EventTarget jsNode = propagationPath.get(i);
                    final EventListenersContainer elc = jsNode.eventListenersContainer_;
                    if (elc != null) {
                        final ScriptResult r = elc.executeCapturingListeners(event, args);
                        result = ScriptResult.combine(r, result, ie);
                        if (event.isPropagationStopped()) {
                            return result;
                        }
                    }
                }
            }

            // at target phase
            event.setEventPhase(Event.AT_TARGET);

            if (!propagationPath.isEmpty()) {
                // Note: This element is not always the same as event.getTarget():
                // e.g. the 'load' event targets Document but "at target" is on Window.
                final EventTarget jsNode = propagationPath.get(0);
                final EventListenersContainer elc = jsNode.eventListenersContainer_;
                if (elc != null) {
                    final ScriptResult r = elc.executeAtTargetListeners(event, args);
                    result = ScriptResult.combine(r, result, ie);
                    if (event.isPropagationStopped()) {
                        return result;
                    }
                }
            }

            // Refactoring note: This should probably be done further down
            HtmlLabel label = null;
            if (event.processLabelAfterBubbling()) {
                for (DomNode parent = ourParentNode; parent != null; parent = parent.getParentNode()) {
                    if (parent instanceof HtmlLabel) {
                        label = (HtmlLabel) parent;
                        break;
                    }
                }
            }

            // bubbling phase
            if (event.isBubbles()) {
                // This belongs here inside the block because events that don't bubble never set
                // eventPhase = 3 (tested in Chrome)
                event.setEventPhase(Event.BUBBLING_PHASE);

                for (int i = 1, size = propagationPath.size(); i < size; i++) {
                    final EventTarget jsNode = propagationPath.get(i);
                    final EventListenersContainer elc = jsNode.eventListenersContainer_;
                    if (elc != null) {
                        final ScriptResult r = elc.executeBubblingListeners(event, args);
                        result = ScriptResult.combine(r, result, ie);
                        if (event.isPropagationStopped()) {
                            return result;
                        }
                    }
                }
            }

            if (label != null) {
                final HtmlElement element = label.getReferencedElement();
                if (element != null && element != getDomNodeOrNull()) {
                    try {
                        element.click(event.isShiftKey(), event.isCtrlKey(), event.isAltKey(), false, true, true);
                    }
                    catch (final IOException e) {
                        // ignore for now
                    }
                }
            }

        }
        finally {
            event.endFire();
            window.setCurrentEvent(previousEvent); // reset event
        }

        return result;
    }

    /**
     * Returns {@code true} if there are any event handlers for the specified event.
     * @param eventName the event name (e.g. "onclick")
     * @return {@code true} if there are any event handlers for the specified event, {@code false} otherwise
     */
    public boolean hasEventHandlers(final String eventName) {
        if (eventListenersContainer_ == null) {
            return false;
        }
        return eventListenersContainer_.hasEventListeners(StringUtils.substring(eventName, 2));
    }

    /**
     * Returns the specified event handler.
     * @param eventType the event type (e.g. "click")
     * @return the handler function, or {@code null} if the property is null or not a function
     */
    public Function getEventHandler(final String eventType) {
        return getEventListenersContainer().getEventHandler(eventType);
    }

    /**
     * Dispatches an event into the event system (standards-conformant browsers only). See
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget/dispatchEvent">the Gecko
     * DOM reference</a> for more information.
     *
     * @param event the event to be dispatched
     * @return {@code false} if at least one of the event handlers which handled the event
     *         called <tt>preventDefault</tt>; {@code true} otherwise
     */
    @JsxFunction
    public boolean dispatchEvent(final Event event) {
        event.setTarget(this);
        final DomElement element = (DomElement) getDomNodeOrNull();
        ScriptResult result = null;
        if (event.getType().equals(MouseEvent.TYPE_CLICK)) {
            try {
                element.click(event, true);
            }
            catch (final IOException e) {
                throw Context.reportRuntimeError("Error calling click(): " + e.getMessage());
            }
        }
        else {
            result = fireEvent(event);
        }
        return !event.isAborted(result);
    }

    /**
     * Allows the removal of event listeners on the event target.
     * @param type the event type to listen for (like "click")
     * @param listener the event listener
     * @param useCapture If {@code true}, indicates that the user wishes to initiate capture (not yet implemented)
     * @see <a href="https://developer.mozilla.org/en-US/docs/DOM/element.removeEventListener">Mozilla
     * documentation</a>
     */
    @JsxFunction
    public void removeEventListener(final String type, final Scriptable listener, final boolean useCapture) {
        getEventListenersContainer().removeEventListener(type, listener, useCapture);
    }

    /**
     * Defines an event handler (or maybe any other object).
     * @param eventName the event name (e.g. "click")
     * @param value the property ({@code null} to reset it)
     */
    public void setEventHandler(final String eventName, final Object value) {
        final EventListenersContainer container;
        if (isEventHandlerOnWindow()) {
            container = getWindow().getEventListenersContainer();
        }
        else {
            container = getEventListenersContainer();
        }
        container.setEventHandler(eventName, value);
    }

    /**
     * Is setting event handler property, at window-level.
     * @return whether the event handler to be set at window-level
     */
    protected boolean isEventHandlerOnWindow() {
        return false;
    }

    /**
     * Clears the event listener container.
     */
    protected void clearEventListenersContainer() {
        eventListenersContainer_ = null;
    }
}
