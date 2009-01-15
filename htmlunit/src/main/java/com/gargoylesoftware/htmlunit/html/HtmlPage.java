/*
 * Copyright (c) 2002-2009 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.html;

import static com.gargoylesoftware.htmlunit.protocol.javascript.JavaScriptURLConnection.JAVASCRIPT_PREFIX;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.ranges.RangeException;

import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.OnbeforeunloadHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.TextUtil;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParser.HtmlUnitDOMBuilder;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.PostponedAction;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import com.gargoylesoftware.htmlunit.javascript.host.Node;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

/**
 * A representation of an HTML page returned from a server.
 * <p>
 * This class provides different methods to access the page's content like
 * {@link #getForms()}, {@link #getAnchors()}, {@link #getElementById(String)}, ... as well as the
 * very powerful inherited methods {@link #getByXPath(String)} and {@link #getFirstByXPath(String)}
 * for fine grained user specific access to child nodes.
 * </p>
 * <p>
 * Child elements allowing user interaction provide methods for this purpose like {@link HtmlAnchor#click()},
 * {@link HtmlInput#type(String)}, {@link HtmlOption#setSelected(boolean)}, ...
 * </p>
 * <p>
 * HtmlPage instances should not be instantiated directly. They will be returned by {@link WebClient#getPage(String)}
 * when the content type of the server's response is <code>text/html</code> (or one of its variations).<br>
 * <br/>
 * <b>Example:</b><br/>
 * <br/>
 * <code>
 * final HtmlPage page = webClient.{@link WebClient#getPage(String) getPage}("http://mywebsite/some/page.html");
 * </code>
 * </p>
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Alex Nikiforoff
 * @author Noboru Sinohara
 * @author David K. Taylor
 * @author Andreas Hangler
 * @author <a href="mailto:cse@dynabean.de">Christian Sell</a>
 * @author Chris Erskine
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Daniel Gredler
 * @author Dmitri Zoubkov
 * @author Sudhan Moghe
 * @author Ethan Glasser-Camp
 */
public final class HtmlPage extends SgmlPage implements Cloneable, Document {

    private HtmlUnitDOMBuilder builder_;
    private static final long serialVersionUID = 1779746292119944291L;

    private String originalCharset_;
    private Map<String, List<HtmlElement>> idMap_ = new HashMap<String, List<HtmlElement>>();
    private Map<String, List<HtmlElement>> nameMap_ = new HashMap<String, List<HtmlElement>>();
    private HtmlElement elementWithFocus_;
    private int parserCount_;
    private int snippetParserCount_;

    private final transient Log javascriptLog_ = LogFactory.getLog("com.gargoylesoftware.htmlunit.javascript");
    private final transient Log mainLog_ = LogFactory.getLog(getClass());

    private List<HtmlAttributeChangeListener> attributeListeners_;
    private final transient Object lock_ = new Object(); // used for synchronization
    private final Range selection_ = new SimpleRange(getDocumentElement());
    private final List<PostponedAction> afterLoadActions_ = new ArrayList<PostponedAction>();

    /**
     * Creates an instance of HtmlPage.
     * An HtmlPage instance is normally retrieved with {@link WebClient#getPage(String)}.
     *
     * @param originatingUrl the URL that was used to load this page
     * @param webResponse the web response that was used to create this page
     * @param webWindow the window that this page is being loaded into
     */
    public HtmlPage(final URL originatingUrl, final WebResponse webResponse, final WebWindow webWindow) {
        super(webResponse, webWindow);
    }

    /**
     * @return this page
     */
    @Override
    public HtmlPage getPage() {
        return this;
    }

    /**
     * Initialize this page.
     * @throws IOException if an IO problem occurs
     * @throws FailingHttpStatusCodeException if the server returns a failing status code AND the property
     * {@link WebClient#setThrowExceptionOnFailingStatusCode(boolean)} is set to true.
     */
    @Override
    public void initialize() throws IOException, FailingHttpStatusCodeException {
        loadFrames();
        setReadyState(READY_STATE_COMPLETE);
        getDocumentElement().setReadyState(READY_STATE_COMPLETE);
        if (!getWebClient().getBrowserVersion().isIE()) {
            executeEventHandlersIfNeeded(Event.TYPE_DOM_DOCUMENT_LOADED);
        }
        executeDeferredScriptsIfNeeded();
        setReadyStateOnDeferredScriptsIfNeeded();
        executeEventHandlersIfNeeded(Event.TYPE_LOAD);
        final List<PostponedAction> actions = new ArrayList<PostponedAction>(afterLoadActions_);
        afterLoadActions_.clear();
        try {
            for (final PostponedAction action : actions) {
                action.execute();
            }
        }
        catch (final IOException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        executeRefreshIfNeeded();
    }

    /**
     * Adds an action that should be executed once the page has been loaded.
     * @param action the action
     */
    void addAfterLoadAction(final PostponedAction action) {
        afterLoadActions_.add(action);
    }

    /**
     * Clean up this page.
     * @throws IOException if an IO problem occurs
     */
    @Override
    public void cleanUp() throws IOException {
        executeEventHandlersIfNeeded(Event.TYPE_UNLOAD);
        deregisterFramesIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlElement getDocumentElement() {
        return (HtmlElement) super.getDocumentElement();
    }

    /**
     * Returns the <tt>body</tt> element (or <tt>frameset</tt> element), or <tt>null</tt> if it does not yet exist.
     * @return the <tt>body</tt> element (or <tt>frameset</tt> element), or <tt>null</tt> if it does not yet exist
     */
    public HtmlElement getBody() {
        final HtmlElement doc = getDocumentElement();
        if (doc != null) {
            for (final DomNode node : doc.getChildren()) {
                if (node instanceof HtmlBody || node instanceof HtmlFrameSet) {
                    return (HtmlElement) node;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getOwnerDocument() {
        return null;
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public org.w3c.dom.Node importNode(final org.w3c.dom.Node importedNode, final boolean deep) {
        throw new UnsupportedOperationException("HtmlPage.importNode is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     */
    public NodeList getElementsByTagName(final String tagName) {
        return new DomNodeList(this, "//*[local-name()='" + tagName + "']");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public NodeList getElementsByTagNameNS(final String namespaceURI, final String localName) {
        throw new UnsupportedOperationException("HtmlPage.getElementsByTagNameNS is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     */
    public HtmlElement getElementById(final String elementId) {
        try {
            return getDocumentElement().getElementById(elementId);
        }
        catch (final ElementNotFoundException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public String getInputEncoding() {
        throw new UnsupportedOperationException("HtmlPage.getInputEncoding is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public String getXmlEncoding() {
        throw new UnsupportedOperationException("HtmlPage.getXmlEncoding is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public boolean getXmlStandalone() {
        throw new UnsupportedOperationException("HtmlPage.getXmlStandalone is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public void setXmlStandalone(final boolean xmlStandalone) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.setXmlStandalone is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public String getXmlVersion() {
        throw new UnsupportedOperationException("HtmlPage.getXmlVersion is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public void setXmlVersion(final String xmlVersion) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.setXmlVersion is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public boolean getStrictErrorChecking() {
        throw new UnsupportedOperationException("HtmlPage.getStrictErrorChecking is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public void setStrictErrorChecking(final boolean strictErrorChecking) {
        throw new UnsupportedOperationException("HtmlPage.setStrictErrorChecking is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public String getDocumentURI() {
        throw new UnsupportedOperationException("HtmlPage.getDocumentURI is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public void setDocumentURI(final String documentURI) {
        throw new UnsupportedOperationException("HtmlPage.setDocumentURI is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public org.w3c.dom.Node adoptNode(final org.w3c.dom.Node source) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.adoptNode is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public DOMConfiguration getDomConfig() {
        throw new UnsupportedOperationException("HtmlPage.getDomConfig is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public void normalizeDocument() {
        throw new UnsupportedOperationException("HtmlPage.normalizeDocument is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public org.w3c.dom.Node renameNode(final org.w3c.dom.Node newNode, final String namespaceURI,
        final String qualifiedName) throws DOMException {
        throw new UnsupportedOperationException("HtmlPage.renameNode is not yet implemented.");
    }

    /**
     * Returns the charset used in the page.
     * The sources of this information are from 1).meta element which
     * http-equiv attribute value is 'content-type', or if not from
     * the response header.
     * @return the value of charset
     */
    @Override
    public String getPageEncoding() {
        // If we've already calculated it, return it.
        if (originalCharset_ != null) {
            return originalCharset_;
        }
        // Try to get the encoding from any <meta> tags.
        for (final HtmlMeta meta : getMetaTags("content-type")) {
            final String contents = meta.getContentAttribute();
            final int pos = contents.toLowerCase().indexOf("charset=");
            if (pos >= 0) {
                final String charset = contents.substring(pos + 8);
                if (charset.length() > 0) {
                    originalCharset_ = charset;
                    if (mainLog_.isDebugEnabled()) {
                        mainLog_.debug("Page Encoding detected: " + originalCharset_);
                    }
                    return originalCharset_;
                }
            }
        }
        // Try to get the encoding from HTTP headers, or based on the content itself.
        if (originalCharset_ == null) {
            originalCharset_ = getWebResponse().getContentCharSet();
        }
        return originalCharset_;
    }

    /**
     * {@inheritDoc}
     * @param tagName the tag name, preferably in lowercase
     */
    @Override
    public HtmlElement createElement(String tagName) {
        if (tagName.indexOf(':') == -1) {
            tagName = tagName.toLowerCase();
        }
        return HTMLParser.getFactory(tagName).createElement(this, tagName, null);
    }

    /**
     * Create a new HTML element with the given tag name.
     *
     * @param tagName the tag name, preferably in lowercase
     * @return the new HTML element
     * @deprecated As of 2.4, please use {@link #createElement(String)} instead.
     */
    @Deprecated
    public HtmlElement createHtmlElement(final String tagName) {
        return createElement(tagName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlElement createElementNS(final String namespaceURI, final String qualifiedName) {
        return HtmlUnitDOMBuilder.getElementFactory(namespaceURI, qualifiedName)
            .createElementNS(this, namespaceURI, qualifiedName, null);
    }

    /**
     * Create a new HtmlElement with the given namespace and qualified name.
     *
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the element type to instantiate
     * @return the new HTML element
     * @deprecated As of 2.4, please use {@link #createElementNS(String,String)} instead
     */
    @Deprecated
    public HtmlElement createHtmlElementNS(final String namespaceURI, final String qualifiedName) {
        return createElementNS(namespaceURI, qualifiedName);
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public Attr createAttributeNS(final String namespaceURI, final String qualifiedName) {
        throw new UnsupportedOperationException("HtmlPage.createAttributeNS is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public Attr createAttribute(final String qualifiedName) {
        throw new UnsupportedOperationException("HtmlPage.createAttribute is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     */
    public Comment createComment(final String data) {
        return new DomComment(this, data);
    }

    /**
     * {@inheritDoc}
     */
    public Text createTextNode(final String data) {
        return new DomText(this, data);
    }

    /**
     * {@inheritDoc}
     */
    public CDATASection createCDATASection(final String data) {
        return new DomCData(this, data);
    }

    /**
     * {@inheritDoc}
     */
    public DocumentFragment createDocumentFragment() {
        return new DomDocumentFragment(this);
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public DOMImplementation getImplementation() {
        throw new UnsupportedOperationException("HtmlPage.getImplementation is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public EntityReference createEntityReference(final String id) {
        throw new UnsupportedOperationException("HtmlPage.createEntityReference is not yet implemented.");
    }

    /**
     * {@inheritDoc}
     * Not yet implemented.
     */
    public ProcessingInstruction createProcessingInstruction(final String namespaceURI, final String qualifiedName) {
        throw new UnsupportedOperationException("HtmlPage.createProcessingInstruction is not yet implemented.");
    }

    /**
     * Returns the {@link HtmlAnchor} with the specified name.
     *
     * @param name the name to search by
     * @return the {@link HtmlAnchor} with the specified name
     * @throws ElementNotFoundException if the anchor could not be found
     */
    public HtmlAnchor getAnchorByName(final String name) throws ElementNotFoundException {
        return getDocumentElement().getOneHtmlElementByAttribute("a", "name", name);
    }

    /**
     * Returns the {@link HtmlAnchor} with the specified href.
     *
     * @param href the string to search by
     * @return the HtmlAnchor
     * @throws ElementNotFoundException if the anchor could not be found
     */
    public HtmlAnchor getAnchorByHref(final String href) throws ElementNotFoundException {
        return getDocumentElement().getOneHtmlElementByAttribute("a", "href", href);
    }

    /**
     * Returns a list of all anchors contained in this page.
     * @return the list of {@link HtmlAnchor} in this page
     */
    public List<HtmlAnchor> getAnchors() {
        return getDocumentElement().getHtmlElementsByTagName("a");
    }

    /**
     * Returns the first anchor that contains the specified text.
     * @param text the text to search for
     * @return the first anchor that was found
     * @throws ElementNotFoundException if no anchors are found with the specified text
     */
    public HtmlAnchor getFirstAnchorByText(final String text) throws ElementNotFoundException {
        WebAssert.notNull("text", text);

        for (final HtmlAnchor anchor : getAnchors()) {
            if (text.equals(anchor.asText())) {
                return anchor;
            }
        }
        throw new ElementNotFoundException("a", "<text>", text);
    }

    /**
     * Returns the first form that matches the specified name.
     * @param name the name to search for
     * @return the first form
     * @exception ElementNotFoundException If no forms match the specified result.
     */
    public HtmlForm getFormByName(final String name) throws ElementNotFoundException {
        final List<HtmlForm> forms = getDocumentElement().getElementsByAttribute("form", "name", name);
        if (forms.size() == 0) {
            throw new ElementNotFoundException("form", "name", name);
        }
        return forms.get(0);
    }

    /**
     * Returns a list of all the forms in this page.
     * @return all the forms in this page
     */
    public List<HtmlForm> getForms() {
        return getDocumentElement().getHtmlElementsByTagName("form");
    }

    /**
     * Given a relative URL (ie <tt>/foo</tt>), returns a fully-qualified URL based on
     * the URL that was used to load this page.
     *
     * @param relativeUrl the relative URL
     * @return the fully-qualified URL for the specified relative URL
     * @exception MalformedURLException If an error occurred when creating a URL object
     */
    public URL getFullyQualifiedUrl(String relativeUrl)
        throws MalformedURLException {

        final List<HtmlBase> baseElements = getDocumentElement().getHtmlElementsByTagName("base");
        URL baseUrl;
        if (baseElements.isEmpty()) {
            baseUrl = getWebResponse().getRequestUrl();
        }
        else {
            if (baseElements.size() > 1) {
                notifyIncorrectness("Multiple 'base' detected, only the first is used.");
            }
            final HtmlBase htmlBase = baseElements.get(0);
            boolean insideHead = false;
            for (DomNode parent = htmlBase.getParentNode(); parent != null; parent = parent.getParentNode()) {
                if (parent instanceof HtmlHead) {
                    insideHead = true;
                    break;
                }
            }

            //http://www.w3.org/TR/1999/REC-html401-19991224/struct/links.html#edef-BASE
            if (!insideHead) {
                notifyIncorrectness("Element 'base' must appear in <head>, it is ignored.");
            }

            final String href = htmlBase.getHrefAttribute();
            if (!insideHead || StringUtils.isEmpty(href)) {
                baseUrl = getWebResponse().getRequestUrl();
            }
            else {
                try {
                    baseUrl = new URL(href);
                }
                catch (final MalformedURLException e) {
                    notifyIncorrectness("Invalid base url: \"" + href + "\", ignoring it");
                    baseUrl = getWebResponse().getRequestUrl();
                }
            }
        }

        // to handle http: and http:/ in FF (Bug 1714767)
        if (getWebClient().getBrowserVersion().isFirefox()) {
            boolean incorrectnessNotified = false;
            while (relativeUrl.startsWith("http:") && !relativeUrl.startsWith("http://")) {
                if (!incorrectnessNotified) {
                    notifyIncorrectness("Incorrect URL \"" + relativeUrl + "\" has been corrected");
                    incorrectnessNotified = true;
                }
                relativeUrl = "http:/" + relativeUrl.substring(5);
            }
        }

        return WebClient.expandUrl(baseUrl, relativeUrl);
    }

    /**
     * Given a target attribute value, resolve the target using a base target for the page.
     *
     * @param elementTarget the target specified as an attribute of the element
     * @return the resolved target to use for the element
     */
    public String getResolvedTarget(final String elementTarget) {
        final List<HtmlBase> baseElements = getDocumentElement().getHtmlElementsByTagName("base");
        final String resolvedTarget;
        if (baseElements.isEmpty()) {
            resolvedTarget = elementTarget;
        }
        else if (elementTarget != null && elementTarget.length() > 0) {
            resolvedTarget = elementTarget;
        }
        else {
            final HtmlBase htmlBase = baseElements.get(0);
            resolvedTarget = htmlBase.getTargetAttribute();
        }
        return resolvedTarget;
    }

    /**
     * Returns a list of ids (strings) that correspond to the tabbable elements
     * in this page. Return them in the same order specified in {@link #getTabbableElements}
     *
     * @return the list of id's
     */
    public List<String> getTabbableElementIds() {
        final List<String> list = new ArrayList<String>();

        for (final HtmlElement element : getTabbableElements()) {
            list.add(element.getAttribute("id"));
        }

        return Collections.unmodifiableList(list);
    }

   /**
    * Returns a list of all elements that are tabbable in the order that will
    * be used for tabbing.<p>
    *
    * The rules for determining tab order are as follows:
    * <ol>
    *   <li>Those elements that support the tabindex attribute and assign a
    *   positive value to it are navigated first. Navigation proceeds from the
    *   element with the lowest tabindex value to the element with the highest
    *   value. Values need not be sequential nor must they begin with any
    *   particular value. Elements that have identical tabindex values should
    *   be navigated in the order they appear in the character stream.
    *   <li>Those elements that do not support the tabindex attribute or
    *   support it and assign it a value of "0" are navigated next. These
    *   elements are navigated in the order they appear in the character
    *   stream.
    *   <li>Elements that are disabled do not participate in the tabbing
    *   order.
    * </ol>
    * Additionally, the value of tabindex must be within 0 and 32767. Any
    * values outside this range will be ignored.<p>
    *
    * The following elements support the <tt>tabindex</tt> attribute: A, AREA, BUTTON,
    * INPUT, OBJECT, SELECT, and TEXTAREA.<p>
    *
    * @return all the tabbable elements in proper tab order
    */
    public List<HtmlElement> getTabbableElements() {
        final List<String> tags = Arrays
            .asList(new String[] {"a", "area", "button", "input", "object", "select", "textarea"});
        final List<HtmlElement> tabbableElements = new ArrayList<HtmlElement>();
        for (final HtmlElement element : getAllHtmlChildElements()) {
            final String tagName = element.getTagName();
            if (tags.contains(tagName)) {
                final boolean disabled = element.hasAttribute("disabled");
                if (!disabled && element.getTabIndex() != HtmlElement.TAB_INDEX_OUT_OF_BOUNDS) {
                    tabbableElements.add(element);
                }
            }
        }
        Collections.sort(tabbableElements, createTabOrderComparator());
        return Collections.unmodifiableList(tabbableElements);
    }

    private Comparator<HtmlElement> createTabOrderComparator() {
        return new Comparator<HtmlElement>() {
            public int compare(final HtmlElement element1, final HtmlElement element2) {
                final Short i1 = element1.getTabIndex();
                final Short i2 = element2.getTabIndex();

                final short index1;
                if (i1 != null) {
                    index1 = i1.shortValue();
                }
                else {
                    index1 = -1;
                }

                final short index2;
                if (i2 != null) {
                    index2 = i2.shortValue();
                }
                else {
                    index2 = -1;
                }

                final int result;
                if (index1 > 0 && index2 > 0) {
                    result = index1 - index2;
                }
                else if (index1 > 0) {
                    result = -1;
                }
                else if (index2 > 0) {
                    result = +1;
                }
                else if (index1 == index2) {
                    result = 0;
                }
                else {
                    result = index2 - index1;
                }

                return result;
            }
        };
    }

    /**
     * Returns the HTML element that is assigned to the specified access key. An
     * access key (aka mnemonic key) is used for keyboard navigation of the
     * page.<p>
     *
     * Only the following HTML elements may have <tt>accesskey</tt>s defined: A, AREA,
     * BUTTON, INPUT, LABEL, LEGEND, and TEXTAREA.
     *
     * @param accessKey the key to look for
     * @return the HTML element that is assigned to the specified key or null
     *      if no elements can be found that match the specified key.
     */
    public HtmlElement getElementByAccessKey(final char accessKey) {
        final List<HtmlElement> elements = getElementsByAccessKey(accessKey);
        if (elements.isEmpty()) {
            return null;
        }
        return elements.get(0);
    }

    /**
     * Returns the HTML element that is assigned to the specified access key. An
     * access key (aka mnemonic key) is used for keyboard navigation of the
     * page.<p>
     *
     * Only the following HTML elements may have <tt>accesskey</tt>s defined: A, AREA,
     * BUTTON, INPUT, LABEL, LEGEND, and TEXTAREA.
     *
     * @param accessKey the key to look for
     * @return the HTML element that is assigned to the specified key or null
     *      if no elements can be found that match the specified key.
     * @deprecated As of 2.4, please use {@link #getElementByAccessKey(char)} instead.
     */
    @Deprecated
    public HtmlElement getHtmlElementByAccessKey(final char accessKey) {
        return getElementByAccessKey(accessKey);
    }

   /**
    * Returns all the HTML elements that are assigned to the specified access key. An
    * access key (aka mnemonic key) is used for keyboard navigation of the
    * page.<p>
    *
    * The HTML specification seems to indicate that one accesskey cannot be used
    * for multiple elements however Internet Explorer does seem to support this.
    * It's worth noting that Mozilla does not support multiple elements with one
    * access key so you are making your HTML browser specific if you rely on this
    * feature.<p>
    *
    * Only the following HTML elements may have <tt>accesskey</tt>s defined: A, AREA,
    * BUTTON, INPUT, LABEL, LEGEND, and TEXTAREA.
    *
    * @param accessKey the key to look for
    * @return the elements that are assigned to the specified accesskey
    */
    public List<HtmlElement> getElementsByAccessKey(final char accessKey) {
        final List<HtmlElement> elements = new ArrayList<HtmlElement>();

        final String searchString = ("" + accessKey).toLowerCase();
        final List<String> acceptableTagNames = Arrays.asList(
                new String[]{"a", "area", "button", "input", "label", "legend", "textarea"});

        for (final HtmlElement element : getAllHtmlChildElements()) {
            if (acceptableTagNames.contains(element.getTagName())) {
                final String accessKeyAttribute = element.getAttribute("accesskey");
                if (searchString.equalsIgnoreCase(accessKeyAttribute)) {
                    elements.add(element);
                }
            }
        }

        return elements;
    }

    /**
     * Returns all the HTML elements that are assigned to the specified access key. An
     * access key (aka mnemonic key) is used for keyboard navigation of the
     * page.<p>
     *
     * The HTML specification seems to indicate that one accesskey cannot be used
     * for multiple elements however Internet Explorer does seem to support this.
     * It's worth noting that Mozilla does not support multiple elements with one
     * access key so you are making your HTML browser specific if you rely on this
     * feature.<p>
     *
     * Only the following HTML elements may have <tt>accesskey</tt>s defined: A, AREA,
     * BUTTON, INPUT, LABEL, LEGEND, and TEXTAREA.
     *
     * @param accessKey the key to look for
     * @return the elements that are assigned to the specified accesskey
     * @deprecated As of 2.4, please use {@link #getElementsByAccessKey(char)} instead.
     */
    @Deprecated
    public List<HtmlElement> getHtmlElementsByAccessKey(final char accessKey) {
        return getElementsByAccessKey(accessKey);
    }

    /**
     * <p>Executes the specified JavaScript code within the page. The usage would be similar to what can
     * be achieved to execute JavaScript in the current page by entering "javascript:...some JS code..."
     * in the URL field of a native browser.</p>
     * <p><b>Note:</b> the provided code won't be executed if JavaScript has been disabled on the WebClient
     * (see {@link WebClient#isJavaScriptEnabled()}.</p>
     * @param sourceCode the JavaScript code to execute
     * @return a ScriptResult which will contain both the current page (which may be different than
     * the previous page) and a JavaScript result object
     */
    public ScriptResult executeJavaScript(final String sourceCode) {
        return executeJavaScriptIfPossible(sourceCode, "injected script", 1);
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * <p>
     * Execute the specified JavaScript if a JavaScript engine was successfully
     * instantiated. If this JavaScript causes the current page to be reloaded
     * (through location="" or form.submit()) then return the new page. Otherwise
     * return the current page.
     * </p>
     * <p><b>Please note:</b> Although this method is public, it is not intended for
     * general execution of JavaScript. Users of HtmlUnit should interact with the pages
     * as a user would by clicking on buttons or links and having the JavaScript event
     * handlers execute as needed..
     * </p>
     *
     * @param sourceCode the JavaScript code to execute
     * @param sourceName the name for this chunk of code (will be displayed in error messages)
     * @param startLine the line at which the script source starts
     * @return a ScriptResult which will contain both the current page (which may be different than
     * the previous page and a JavaScript result object.
     */
    public ScriptResult executeJavaScriptIfPossible(String sourceCode, final String sourceName, final int startLine) {
        if (!getWebClient().isJavaScriptEnabled()) {
            return new ScriptResult(null, this);
        }

        final int prefixLength = JAVASCRIPT_PREFIX.length();

        if (sourceCode.length() > prefixLength
                && sourceCode.substring(0, prefixLength).equalsIgnoreCase(JAVASCRIPT_PREFIX)) {
            sourceCode = sourceCode.substring(prefixLength);
        }

        final Object result = getWebClient().getJavaScriptEngine().execute(this, sourceCode, sourceName, startLine);

        return new ScriptResult(result, getWebClient().getCurrentWindow().getEnclosedPage());
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * Execute a Function in the given context.
     *
     * @param function the JavaScript Function to call
     * @param thisObject the "this" object to be used during invocation
     * @param args the arguments to pass into the call
     * @param htmlElementScope the HTML element for which this script is being executed
     * This element will be the context during the JavaScript execution. If null,
     * the context will default to the page.
     * @return a ScriptResult which will contain both the current page (which may be different than
     * the previous page and a JavaScript result object.
     */
    public ScriptResult executeJavaScriptFunctionIfPossible(final Function function, final Scriptable thisObject,
            final Object[] args, final DomNode htmlElementScope) {

        if (!getWebClient().isJavaScriptEnabled()) {
            return new ScriptResult(null, this);
        }

        final JavaScriptEngine engine = getWebClient().getJavaScriptEngine();
        final Object result = engine.callFunction(this, function, thisObject, args, htmlElementScope);

        return new ScriptResult(result, getWebClient().getCurrentWindow().getEnclosedPage());
    }

    /**
     * Returns the log object for this element.
     * @return the log object for this element
     */
    protected Log getJsLog() {
        return javascriptLog_;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * @param srcAttribute the source attribute from the script tag
     * @param charset the charset attribute from the script tag
     */
    void loadExternalJavaScriptFile(final String srcAttribute, final String charset) {
        if (getWebClient().isJavaScriptEnabled()) {
            final URL scriptURL;
            try {
                scriptURL = getFullyQualifiedUrl(srcAttribute);
                if (scriptURL.getProtocol().equals("javascript")) {
                    if (mainLog_.isInfoEnabled()) {
                        mainLog_.info("Ignoring script src [" + srcAttribute + "]");
                    }
                    return;
                }
            }
            catch (final MalformedURLException e) {
                if (mainLog_.isErrorEnabled()) {
                    mainLog_.error("Unable to build URL for script src tag [" + srcAttribute + "]");
                }
                if (getWebClient().isThrowExceptionOnScriptError()) {
                    throw new ScriptException(this, e);
                }
                return;
            }

            final Script script = loadJavaScriptFromUrl(scriptURL, charset);
            if (script != null) {
                getWebClient().getJavaScriptEngine().execute(this, script);
            }
        }
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * Return true if a script with the specified type and language attributes
     * is actually JavaScript.
     * According to <a href="http://www.w3.org/TR/REC-html40/types.html#h-6.7">W3C recommendation</a>
     * are content types case insensitive.
     * @param typeAttribute the type attribute specified in the script tag
     * @param languageAttribute the language attribute specified in the script tag
     * @return true if the script is JavaScript
     */
    public static boolean isJavaScript(final String typeAttribute, final String languageAttribute) {
        // Unless otherwise specified, we have to assume that any script is JavaScript
        final boolean isJavaScript;
        if (languageAttribute != null && languageAttribute.length() != 0) {
            isJavaScript = TextUtil.startsWithIgnoreCase(languageAttribute, "javascript");
        }
        else if (typeAttribute != null && typeAttribute.length() != 0) {
            isJavaScript = typeAttribute.equalsIgnoreCase("text/javascript");
        }
        else {
            isJavaScript = true;
        }

        return isJavaScript;
    }

    /**
     * Loads JavaScript from the specified URL. This method may return <tt>null</tt> if
     * there is a problem loading the code from the specified URL.
     *
     * @param url the URL of the script
     * @param charset the charset to use to read the text
     * @return the content of the file
     */
    private Script loadJavaScriptFromUrl(final URL url, final String charset) {
        String scriptEncoding = charset;
        final String pageEncoding = getPageEncoding();

        final WebClient client = getWebClient();
        final Cache cache = client.getCache();

        final WebRequestSettings request = new WebRequestSettings(url);

        final WebRequestSettings referringRequest = getWebResponse().getRequestSettings();
        request.setAdditionalHeaders(new HashMap<String, String>(referringRequest.getAdditionalHeaders()));
        request.addAdditionalHeader("Referer", referringRequest.getUrl().toString());
        final Script cachedScript = cache.getCachedScript(request);
        if (cachedScript != null) {
            return cachedScript;
        }

        WebResponse response;
        try {
            response = client.loadWebResponse(request);
        }
        catch (final IOException e) {
            if (mainLog_.isErrorEnabled()) {
                mainLog_.error("Error loading JavaScript from [" + url + "].", e);
            }
            return null;
        }

        client.printContentIfNecessary(response);
        client.throwFailingHttpStatusCodeExceptionIfNecessary(response);

        final int statusCode = response.getStatusCode();
        final boolean successful = (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES);
        if (!successful) {
            return null;
        }

        //http://www.ietf.org/rfc/rfc4329.txt
        final String contentType = response.getContentType();
        if (!contentType.equalsIgnoreCase("application/javascript")
            && !contentType.equalsIgnoreCase("application/ecmascript")) {
            if (contentType.equals("text/javascript") || contentType.equals("text/ecmascript")) {
                getWebClient().getIncorrectnessListener().notify(
                    "Obsolete content type encountered: '" + contentType + "'.", this);
            }
            else {
                getWebClient().getIncorrectnessListener().notify(
                        "Expected content type of 'application/javascript' or 'application/ecmascript' for "
                        + "remotely loaded JavaScript element at '" + url + "', "
                        + "but got '" + contentType + "'.", this);
            }
        }

        if (StringUtils.isEmpty(scriptEncoding)) {
            final String contentCharset = response.getContentCharSet();
            if (!contentCharset.equals(TextUtil.DEFAULT_CHARSET)) {
                scriptEncoding = contentCharset;
            }
            else if (!pageEncoding.equals(TextUtil.DEFAULT_CHARSET)) {
                scriptEncoding = pageEncoding;
            }
            else {
                scriptEncoding = TextUtil.DEFAULT_CHARSET;
            }
        }

        final String scriptCode = response.getContentAsString(scriptEncoding);
        final JavaScriptEngine javaScriptEngine = client.getJavaScriptEngine();
        final Script script = javaScriptEngine.compile(this, scriptCode, url.toExternalForm(), 1);
        cache.cacheIfPossible(request, response, script);
        return script;
    }

    /**
     * Returns the title of this page or an empty string if the title wasn't specified.
     *
     * @return the title of this page or an empty string if the title wasn't specified
     */
    public String getTitleText() {
        final HtmlTitle titleElement = getTitleElement();
        if (titleElement != null) {
            return titleElement.asText();
        }
        return "";
    }

    /**
     * Sets the text for the title of this page. If there is not a title element
     * on this page, then one has to be generated.
     * @param message the new text
     */
    public void setTitleText(final String message) {
        HtmlTitle titleElement = getTitleElement();
        if (titleElement == null) {
            if (mainLog_.isDebugEnabled()) {
                mainLog_.debug("No title element, creating one");
            }
            final HtmlHead head = (HtmlHead) getFirstChildElement(getDocumentElement(), HtmlHead.class);
            if (head == null) {
                // perhaps should we create head too?
                throw new IllegalStateException("Headelement was not defined for this page");
            }
            final Map<String, DomAttr> emptyMap = Collections.emptyMap();
            titleElement = new HtmlTitle(null, HtmlTitle.TAG_NAME, this, emptyMap);
            if (head.getFirstChild() != null) {
                head.getFirstChild().insertBefore(titleElement);
            }
            else {
                head.appendChild(titleElement);
            }
        }

        titleElement.setNodeValue(message);
    }

    /**
     * Gets the first child of startElement that is an instance of the given class.
     * @param startElement the parent element
     * @param clazz the class to search for
     * @return <code>null</code> if no child found
     */
    private HtmlElement getFirstChildElement(final HtmlElement startElement, final Class< ? > clazz) {
        for (final HtmlElement element : startElement.getChildElements()) {
            if (clazz.isInstance(element)) {
                return element;
            }
        }

        return null;
    }

    /**
     * Gets the title element for this page. Returns null if one is not found.
     *
     * @return the title element for this page or null if this is not one
     */
    private HtmlTitle getTitleElement() {
        final HtmlHead head = (HtmlHead) getFirstChildElement(getDocumentElement(), HtmlHead.class);
        if (head != null) {
            return (HtmlTitle) getFirstChildElement(head, HtmlTitle.class);
        }

        return null;
    }

    /**
     * Looks for and executes any appropriate event handlers. Looks for body and frame tags.
     * @param eventType either {@link Event#TYPE_LOAD}, {@link Event#TYPE_UNLOAD}, or {@link Event#TYPE_BEFORE_UNLOAD}
     * @return <tt>true</tt> if user accepted <tt>onbeforeunload</tt> (not relevant to other events)
     */
    private boolean executeEventHandlersIfNeeded(final String eventType) {
        // If JavaScript isn't enabled, there's nothing for us to do.
        if (!getWebClient().isJavaScriptEnabled()) {
            return true;
        }

        // Execute the specified event on the document element.
        final WebWindow window = getEnclosingWindow();
        final Window jsWindow = (Window) window.getScriptObject();
        if (jsWindow != null) {
            final HtmlElement element = getDocumentElement();
            final Event event = new Event(element, eventType);
            element.fireEvent(event);
            if (!isOnbeforeunloadAccepted(this, event)) {
                return false;
            }
        }

        // If this page was loaded in a frame, execute the version of the event specified on the frame tag.
        if (window instanceof FrameWindow) {
            final FrameWindow fw = (FrameWindow) window;
            final BaseFrame frame = fw.getFrameElement();
            final Function frameTagEventHandler = frame.getEventHandler("on" + eventType);
            if (frameTagEventHandler != null) {
                if (mainLog_.isDebugEnabled()) {
                    mainLog_.debug("Executing on" + eventType + " handler for " + frame);
                }
                final Event event = new Event(frame, eventType);
                ((Node) frame.getScriptObject()).executeEvent(event);
                if (!isOnbeforeunloadAccepted((HtmlPage) frame.getPage(), event)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isOnbeforeunloadAccepted(final HtmlPage page, final Event event) {
        if (event.jsxGet_type().equals(Event.TYPE_BEFORE_UNLOAD) && event.jsxGet_returnValue() != null) {
            final OnbeforeunloadHandler handler = getWebClient().getOnbeforeunloadHandler();
            if (handler == null) {
                if (mainLog_.isWarnEnabled()) {
                    mainLog_.warn("document.onbeforeunload() returned a string in event.returnValue,"
                            + " but no onbeforeunload handler installed.");
                }
            }
            else {
                final String message = Context.toString(event.jsxGet_returnValue());
                return handler.handleEvent(page, message);
            }
        }
        return true;
    }

    /**
     * If a refresh has been specified either through a meta tag or an HTTP
     * response header, then perform that refresh.
     * @throws IOException if an IO problem occurs
     */
    private void executeRefreshIfNeeded() throws IOException {
        // If this page is not in a frame then a refresh has already happened,
        // most likely through the JavaScript onload handler, so we don't do a
        // second refresh.
        final WebWindow window = getEnclosingWindow();
        if (window == null) {
            return;
        }

        final String refreshString = getRefreshStringOrNull();
        if (refreshString == null || refreshString.length() == 0) {
            return;
        }

        final int time;
        final URL url;

        int index = refreshString.indexOf(";");
        final boolean timeOnly = (index == -1);

        if (timeOnly) {
            // Format: <meta http-equiv='refresh' content='10'>
            try {
                time = Integer.parseInt(refreshString);
            }
            catch (final NumberFormatException e) {
                if (mainLog_.isErrorEnabled()) {
                    mainLog_.error("Malformed refresh string (no ';' but not a number): " + refreshString, e);
                }
                return;
            }
            url = getWebResponse().getRequestUrl();
        }
        else {
            // Format: <meta http-equiv='refresh' content='10;url=http://www.blah.com'>
            try {
                time = Integer.parseInt(refreshString.substring(0, index).trim());
            }
            catch (final NumberFormatException e) {
                if (mainLog_.isErrorEnabled()) {
                    mainLog_.error("Malformed refresh string (no valid number before ';') " + refreshString, e);
                }
                return;
            }
            index = refreshString.toLowerCase().indexOf("url=", index);
            if (index == -1) {
                if (mainLog_.isErrorEnabled()) {
                    mainLog_.error("Malformed refresh string (found ';' but no 'url='): " + refreshString);
                }
                return;
            }
            final StringBuilder buffer = new StringBuilder(refreshString.substring(index + 4));
            if (buffer.toString().trim().length() == 0) {
                //content='10; URL=' is treated as content='10'
                url = getWebResponse().getRequestUrl();
            }
            else {
                if (buffer.charAt(0) == '"' || buffer.charAt(0) == 0x27) {
                    buffer.deleteCharAt(0);
                }
                if (buffer.charAt(buffer.length() - 1) == '"' || buffer.charAt(buffer.length() - 1) == 0x27) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }
                final String urlString = buffer.toString();
                try {
                    url = getFullyQualifiedUrl(urlString);
                }
                catch (final MalformedURLException e) {
                    if (mainLog_.isErrorEnabled()) {
                        mainLog_.error("Malformed URL in refresh string: " + refreshString, e);
                    }
                    throw e;
                }
            }
        }

        getWebClient().getRefreshHandler().handleRefresh(this, url, time);
    }

    /**
     * Returns an auto-refresh string if specified. This will look in both the meta
     * tags (taking care of &lt;noscript&gt; if any) and inside the HTTP response headers.
     * @return the auto-refresh string
     */
    private String getRefreshStringOrNull() {
        final boolean javaScriptEnabled = getWebClient().isJavaScriptEnabled();
        for (final HtmlMeta meta : getMetaTags("refresh")) {
            if ((!javaScriptEnabled || getFirstParent(meta, HtmlNoScript.TAG_NAME) == null)) {
                return meta.getContentAttribute();
            }
        }
        return getWebResponse().getResponseHeaderValue("Refresh");
    }

    /**
     * Executes any deferred scripts, if necessary.
     */
    private void executeDeferredScriptsIfNeeded() {
        if (!getWebClient().isJavaScriptEnabled()) {
            return;
        }
        if (!getWebClient().getBrowserVersion().isIE()) {
            return;
        }
        final HtmlElement doc = getDocumentElement();
        final List<HtmlElement> elements = doc.getHtmlElementsByTagName("script");
        for (final HtmlElement e : elements) {
            if (e instanceof HtmlScript) {
                final HtmlScript script = (HtmlScript) e;
                if (script.isDeferred()) {
                    script.executeScriptIfNeeded(true);
                }
            }
        }
    }

    /**
     * Sets the ready state on any deferred scripts, if necessary.
     */
    private void setReadyStateOnDeferredScriptsIfNeeded() {
        if (getWebClient().isJavaScriptEnabled() && getWebClient().getBrowserVersion().isIE()) {
            final List<HtmlElement> elements = getDocumentElement().getHtmlElementsByTagName("script");
            for (final HtmlElement e : elements) {
                if (e instanceof HtmlScript) {
                    final HtmlScript script = (HtmlScript) e;
                    if (script.isDeferred()) {
                        script.setAndExecuteReadyState(READY_STATE_COMPLETE);
                    }
                }
            }
        }
    }

    /**
     * Returns the first parent with the specified node name, or <tt>null</tt> if no parent
     * with the specified node name can be found.
     *
     * @param node the node to start with
     * @param nodeName the name of the search node
     * @return the first parent with the specified node name
     */
    private DomNode getFirstParent(final DomNode node, final String nodeName) {
        DomNode parent = node.getParentNode();
        while (parent != null) {
            if (parent.getNodeName().equals(nodeName)) {
                return parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }

    /**
     * Deregister frames that are no longer in use.
     */
    public void deregisterFramesIfNeeded() {
        for (final WebWindow window : getFrames()) {
            getWebClient().deregisterWebWindow(window);
            if (window.getEnclosedPage() instanceof HtmlPage) {
                final HtmlPage page = (HtmlPage) window.getEnclosedPage();
                if (page != null) {
                    // seems quite silly, but for instance if the src attribute of an iframe is not
                    // set, the error only occurs when leaving the page
                    page.deregisterFramesIfNeeded();
                }
            }
        }
    }

    /**
     * Returns a list containing all the frames (from frame and iframe tags) in this page.
     * @return a list of {@link FrameWindow}
     */
    public List<FrameWindow> getFrames() {
        final List<FrameWindow> list = new ArrayList<FrameWindow>();
        final WebWindow enclosingWindow = getEnclosingWindow();
        for (final WebWindow window : getWebClient().getWebWindows()) {
            // quite strange but for a TopLevelWindow parent == self
            if (enclosingWindow == window.getParentWindow()
                    && enclosingWindow != window) {
                list.add((FrameWindow) window);
            }
        }
        return list;
    }

    /**
     * Returns the first frame contained in this page with the specified name.
     * @param name the name to search for
     * @return the first frame found
     * @exception ElementNotFoundException If no frame exist in this page with the specified name.
     */
    public FrameWindow getFrameByName(final String name) throws ElementNotFoundException {
        for (final FrameWindow frame : getFrames()) {
            if (frame.getName().equals(name)) {
                return frame;
            }
        }

        throw new ElementNotFoundException("frame or iframe", "name", name);
    }

    /**
     * Simulate pressing an access key. This may change the focus, may click buttons and may invoke
     * JavaScript.
     *
     * @param accessKey the key that will be pressed
     * @return the element that has the focus after pressing this access key or null if no element
     * has the focus.
     * @throws IOException if an IO error occurs during the processing of this access key (this
     *         would only happen if the access key triggered a button which in turn caused a page load)
     */
    public HtmlElement pressAccessKey(final char accessKey) throws IOException {
        final HtmlElement element = getElementByAccessKey(accessKey);
        if (element != null) {
            element.focus();
            final Page newPage;
            if (element instanceof HtmlAnchor) {
                newPage = ((HtmlAnchor) element).click();
            }
            else if (element instanceof HtmlArea) {
                newPage = ((HtmlArea) element).click();
            }
            else if (element instanceof HtmlButton) {
                newPage = ((HtmlButton) element).click();
            }
            else if (element instanceof HtmlInput) {
                newPage = ((HtmlInput) element).click();
            }
            else if (element instanceof HtmlLabel) {
                newPage = ((HtmlLabel) element).click();
            }
            else if (element instanceof HtmlLegend) {
                newPage = ((HtmlLegend) element).click();
            }
            else if (element instanceof HtmlTextArea) {
                newPage = ((HtmlTextArea) element).click();
            }
            else {
                newPage = this;
            }

            if (newPage != this && getFocusedElement() == element) {
                // The page was reloaded therefore no element on this page will have the focus.
                getFocusedElement().blur();
            }
        }

        return getFocusedElement();
    }

    /**
     * Move the focus to the next element in the tab order. To determine the specified tab
     * order, refer to {@link HtmlPage#getTabbableElements()}
     *
     * @return the element that has focus after calling this method
     */
    public HtmlElement tabToNextElement() {
        final List<HtmlElement> elements = getTabbableElements();
        if (elements.isEmpty()) {
            setFocusedElement(null);
            return null;
        }

        final HtmlElement elementToGiveFocus;
        final HtmlElement elementWithFocus = getFocusedElement();
        if (elementWithFocus == null) {
            elementToGiveFocus = elements.get(0);
        }
        else {
            final int index = elements.indexOf(elementWithFocus);
            if (index == -1) {
                // The element with focus isn't on this page
                elementToGiveFocus = elements.get(0);
            }
            else {
                if (index == elements.size() - 1) {
                    elementToGiveFocus = elements.get(0);
                }
                else {
                    elementToGiveFocus = elements.get(index + 1);
                }
            }
        }

        setFocusedElement(elementToGiveFocus);
        return elementToGiveFocus;
    }

    /**
     * Move the focus to the previous element in the tab order. To determine the specified tab
     * order, refer to {@link HtmlPage#getTabbableElements()}
     *
     * @return the element that has focus after calling this method
     */
    public HtmlElement tabToPreviousElement() {
        final List<HtmlElement> elements = getTabbableElements();
        if (elements.isEmpty()) {
            setFocusedElement(null);
            return null;
        }

        final HtmlElement elementToGiveFocus;
        final HtmlElement elementWithFocus = getFocusedElement();
        if (elementWithFocus == null) {
            elementToGiveFocus = elements.get(elements.size() - 1);
        }
        else {
            final int index = elements.indexOf(elementWithFocus);
            if (index == -1) {
                // The element with focus isn't on this page
                elementToGiveFocus = elements.get(elements.size() - 1);
            }
            else {
                if (index == 0) {
                    elementToGiveFocus = elements.get(elements.size() - 1);
                }
                else {
                    elementToGiveFocus = elements.get(index - 1);
                }
            }
        }

        setFocusedElement(elementToGiveFocus);
        return elementToGiveFocus;
    }

    /**
     * Returns the HTML element with the specified ID. If more than one element
     * has this ID (not allowed by the HTML spec), then this method returns the
     * first one.
     *
     * @param id the ID value to search by
     * @param <E> the sub-element type
     * @return the HTML element with the specified ID
     * @throws ElementNotFoundException if no element was found that matches the id
     */
    @SuppressWarnings("unchecked")
    public <E extends HtmlElement> E getHtmlElementById(final String id) throws ElementNotFoundException {
        final List<HtmlElement> elements = idMap_.get(id);
        if (elements != null) {
            return (E) elements.get(0);
        }
        throw new ElementNotFoundException("*", "id", id);
    }

    /**
     * Returns the HTML elements with the specified name attribute. If there are no elements
     * with the specified name, this method returns an empty list. Please note that
     * the lists returned by this method are immutable.
     *
     * @param name the name value to search by
     * @return the HTML elements with the specified name attribute
     */
    public List<HtmlElement> getElementsByName(final String name) {
        final List<HtmlElement> list = nameMap_.get(name);
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the HTML elements with the specified name attribute. If there are no elements
     * with the specified name, this method returns an empty list. Please note that
     * the lists returned by this method are immutable.
     *
     * @param name the name value to search by
     * @return the HTML elements with the specified name attribute
     * @deprecated As of 2.4, please use {@link #getElementsByName(String)} instead
     */
    @Deprecated
    public List<HtmlElement> getHtmlElementsByName(final String name) {
        return getElementsByName(name);
    }

    /**
     * Returns the HTML elements with the specified string for their name or ID. If there are
     * no elements with the specified name or ID, this method returns an empty list. Please note
     * that lists returned by this method are immutable.
     *
     * @param idAndOrName the value to search for
     * @return the HTML elements with the specified string for their name or ID
     */
    public List<HtmlElement> getElementsByIdAndOrName(final String idAndOrName) {
        final List<HtmlElement> list1 = idMap_.get(idAndOrName);
        final List<HtmlElement> list2 = nameMap_.get(idAndOrName);
        final List<HtmlElement> list = new ArrayList<HtmlElement>();
        if (list1 != null) {
            list.addAll(list1);
        }
        if (list2 != null) {
            for (final HtmlElement elt : list2) {
                if (!list.contains(elt)) {
                    list.add(elt);
                }
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the HTML elements with the specified string for their name or ID. If there are
     * no elements with the specified name or ID, this method returns an empty list. Please note
     * that lists returned by this method are immutable.
     *
     * @param idAndOrName the value to search for
     * @return the HTML elements with the specified string for their name or ID
     * @deprecated As of 2.4, please use {@link #getElementsByIdAndOrName(String)} instead
     */
    @Deprecated
    public List<HtmlElement> getHtmlElementsByIdAndOrName(final String idAndOrName) {
        return getElementsByIdAndOrName(idAndOrName);
    }

    /**
     * Adds an element to the ID and name maps, if necessary.
     * @param element the element to be added to the ID and name maps
     */
    void addMappedElement(final HtmlElement element) {
        addMappedElement(element, false);
    }

    /**
     * Adds an element to the ID and name maps, if necessary.
     * @param element the element to be added to the ID and name maps
     * @param recurse indicates if children must be added too
     */
    void addMappedElement(final HtmlElement element, final boolean recurse) {
        if (isDescendant(element)) {
            addElement(idMap_, element, "id", recurse);
            addElement(nameMap_, element, "name", recurse);
        }
    }

    /**
     * Checks whether the specified element is descendant of this HtmlPage or not.
     */
    private boolean isDescendant(final HtmlElement element) {
        for (DomNode parent = element; parent != null; parent = parent.getParentNode()) {
            if (parent == this) {
                return true;
            }
        }
        return false;
    }

    private void addElement(final Map<String, List<HtmlElement>> map, final HtmlElement element,
            final String attribute, final boolean recurse) {
        final String value = element.getAttribute(attribute);
        if (!StringUtils.isEmpty(value)) {
            List<HtmlElement> elements = map.get(value);
            if (elements == null) {
                elements = new ArrayList<HtmlElement>();
                elements.add(element);
                map.put(value, elements);
            }
            else if (!elements.contains(element)) {
                elements.add(element);
            }
        }
        if (recurse) {
            for (final HtmlElement child : element.getChildElements()) {
                addElement(map, child, attribute, true);
            }
        }
    }

    /**
     * Removes an element from the ID and name maps, if necessary.
     * @param element the element to be removed from the ID and name maps
     */
    void removeMappedElement(final HtmlElement element) {
        removeMappedElement(element, false, false);
    }

    /**
     * Removes an element and optionally its children from the ID and name maps, if necessary.
     * @param element the element to be removed from the ID and name maps
     * @param recurse indicates if children must be removed too
     * @param descendant indicates of the element was descendant of this HtmlPage, but now its parent might be null
     */
    void removeMappedElement(final HtmlElement element, final boolean recurse, final boolean descendant) {
        if (descendant || isDescendant(element)) {
            removeElement(idMap_, element, "id", recurse);
            removeElement(nameMap_, element, "name", recurse);
        }
    }

    private void removeElement(final Map<String, List<HtmlElement>> map, final HtmlElement element, final String att,
            final boolean recurse) {
        final String value = element.getAttribute(att);
        if (!StringUtils.isEmpty(value)) {
            final List<HtmlElement> elements = map.remove(value);
            if (elements != null && (elements.size() != 1 || !elements.contains(element))) {
                elements.remove(element);
                map.put(value, elements);
            }
        }
        if (recurse) {
            for (final HtmlElement child : element.getChildElements()) {
                removeElement(map, child, att, true);
            }
        }
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * @param node the node that has just been added to the document
     */
    void notifyNodeAdded(final DomNode node) {
        if (node instanceof HtmlElement) {
            boolean insideNoScript = false;
            if (getWebClient().isJavaScriptEnabled()) {
                for (DomNode parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
                    if (parent instanceof HtmlNoScript) {
                        insideNoScript = true;
                        break;
                    }
                }
            }
            if (!insideNoScript) {
                addMappedElement((HtmlElement) node, true);
            }
        }
        node.onAddedToPage();
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * @param node the node that has just been removed from the tree
     */
    void notifyNodeRemoved(final DomNode node) {
        if (node instanceof HtmlElement) {
            removeMappedElement((HtmlElement) node, true, true);
        }
    }

    /**
     * Loads the content of the contained frames. This is done after the page is completely loaded, to allow script
     * contained in the frames to reference elements from the page located after the closing &lt;/frame&gt; tag.
     * @throws FailingHttpStatusCodeException if the server returns a failing status code AND the property
     *         {@link WebClient#setThrowExceptionOnFailingStatusCode(boolean)} is set to <tt>true</tt>
     */
    void loadFrames() throws FailingHttpStatusCodeException {
        for (final FrameWindow w : getFrames()) {
            final BaseFrame frame = w.getFrameElement();
            // test if the frame should really be loaded:
            // if a script has already changed its content, it should be skipped
            // use == and not equals(...) to identify initial content (versus URL set to "about:blank")
            if (frame.getEnclosedPage().getWebResponse().getRequestUrl() == WebClient.URL_ABOUT_BLANK) {
                frame.loadInnerPage();
            }
        }
    }

    /**
     * Gives a basic representation for debugging purposes.
     * @return a basic representation
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("HtmlPage(");
        buffer.append(getWebResponse().getRequestUrl());
        buffer.append(")@");
        buffer.append(hashCode());
        return buffer.toString();
    }

    /**
     * Moves the focus to the specified element. This will trigger any relevant JavaScript
     * event handlers.
     *
     * @param newElement the element that will receive the focus, use <code>null</code> to remove focus from any element
     * @return true if the specified element now has the focus
     * @see #getFocusedElement()
     * @see #tabToNextElement()
     * @see #tabToPreviousElement()
     * @see #pressAccessKey(char)
     * @see WebAssert#assertAllTabIndexAttributesSet(HtmlPage)
     */
    public boolean setFocusedElement(final HtmlElement newElement) {
        return setFocusedElement(newElement, false);
    }

    /**
     * Moves the focus to the specified element. This will trigger any relevant JavaScript
     * event handlers.
     *
     * @param newElement the element that will receive the focus, use <code>null</code> to remove focus from any element
     * @param windowActivated - whether the enclosing window got focus resulting in specified element getting focus
     * @return true if the specified element now has the focus
     * @see #getFocusedElement()
     * @see #tabToNextElement()
     * @see #tabToPreviousElement()
     * @see #pressAccessKey(char)
     * @see WebAssert#assertAllTabIndexAttributesSet(HtmlPage)
     */
    public boolean setFocusedElement(final HtmlElement newElement, final boolean windowActivated) {
        if (elementWithFocus_ == newElement && (!windowActivated)) {
            // nothing to do
            return true;
        }
        else if (newElement != null && newElement.getPage() != this) {
            throw new IllegalArgumentException("Can't move focus to an element from an other page");
        }

        if (!windowActivated) {
            if (elementWithFocus_ != null) {
                elementWithFocus_.fireEvent(Event.TYPE_FOCUS_OUT);
            }

            if (newElement != null) {
                newElement.fireEvent(Event.TYPE_FOCUS_IN);
            }

            if (elementWithFocus_ != null) {
                if (getWebClient().getBrowserVersion().hasFeature(BrowserVersionFeatures.BLUR_BEFORE_ONCHANGE)) {
                    elementWithFocus_.fireEvent(Event.TYPE_BLUR);
                    elementWithFocus_.removeFocus();
                }
                else { // IE, FF3
                    elementWithFocus_.removeFocus();
                    elementWithFocus_.fireEvent(Event.TYPE_BLUR);
                }
            }
        }

        elementWithFocus_ = newElement;

        if (newElement != null) {
            elementWithFocus_.focus();
            newElement.fireEvent(Event.TYPE_FOCUS);
        }

        // If a page reload happened as a result of the focus change then obviously this
        // element will not have the focus because its page has gone away.
        return this == getEnclosingWindow().getEnclosedPage();
    }

    /**
     * Returns the element with the focus or null if no element has the focus.
     * @return the element with focus or null
     * @see #setFocusedElement(HtmlElement)
     */
    public HtmlElement getFocusedElement() {
        return elementWithFocus_;
    }

    /**
     * Gets the meta tag for a given http-equiv value.
     * @param httpEquiv the http-equiv value
     * @return a list of {@link HtmlMeta}
     */
    protected List<HtmlMeta> getMetaTags(final String httpEquiv) {
        final String nameLC = httpEquiv.toLowerCase();
        final List<HtmlMeta> tags = getDocumentElement().getHtmlElementsByTagName("meta");
        for (final Iterator<HtmlMeta> iter = tags.iterator(); iter.hasNext();) {
            final HtmlMeta element = iter.next();
            if (!nameLC.equals(element.getHttpEquivAttribute().toLowerCase())) {
                iter.remove();
            }
        }
        return tags;
    }

    /**
     * Select the specified radio button in the page (outside any &lt;form&gt;).
     *
     * @param radioButtonInput the radio Button
     */
    @SuppressWarnings("unchecked")
    void setCheckedRadioButton(final HtmlRadioButtonInput radioButtonInput) {
        // May be done in single XPath search?
        final List<HtmlRadioButtonInput> pageInputs =
            (List<HtmlRadioButtonInput>) getByXPath("//input[lower-case(@type)='radio' "
                + "and @name='" + radioButtonInput.getNameAttribute() + "']");
        final List<HtmlRadioButtonInput> formInputs =
            (List<HtmlRadioButtonInput>) getByXPath("//form//input[lower-case(@type)='radio' "
                + "and @name='" + radioButtonInput.getNameAttribute() + "']");

        pageInputs.removeAll(formInputs);

        for (final HtmlRadioButtonInput input : pageInputs) {
            if (input == radioButtonInput) {
                input.setAttribute("checked", "checked");
            }
            else {
                input.removeAttribute("checked");
            }
        }
    }

    /**
     * Creates a clone of this instance, and clears cached state
     * to be not shared with the original.
     *
     * @return a clone of this instance
     */
    @Override
    protected HtmlPage clone() {
        final HtmlPage result = (HtmlPage) super.clone();
        result.elementWithFocus_ = null;
        result.idMap_ = new HashMap<String, List<HtmlElement>>();
        result.nameMap_ = new HashMap<String, List<HtmlElement>>();
        return result;
    }

    /**
     * {@inheritDoc}
     * Override cloneNode to add cloned elements to the clone, not to the original.
     */
    @Override
    public HtmlPage cloneNode(final boolean deep) {
        final HtmlPage result = (HtmlPage) super.cloneNode(deep);
        result.setScriptObject(getScriptObject());
        if (deep) {
            // fix up idMap_ and result's idMap_s
            for (final HtmlElement child : result.getAllHtmlChildElements()) {
                removeMappedElement(child);
                result.addMappedElement(child);
            }
        }
        return result;
    }

    /**
     * Adds an HtmlAttributeChangeListener to the listener list.
     * The listener is registered for all attributes of all HtmlElements contained in this page.
     *
     * @param listener the attribute change listener to be added
     * @see #removeHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    public void addHtmlAttributeChangeListener(final HtmlAttributeChangeListener listener) {
        WebAssert.notNull("listener", listener);
        synchronized (lock_) {
            if (attributeListeners_ == null) {
                attributeListeners_ = new ArrayList<HtmlAttributeChangeListener>();
            }
            if (!attributeListeners_.contains(listener)) {
                attributeListeners_.add(listener);
            }
        }
    }

    /**
     * Removes an HtmlAttributeChangeListener from the listener list.
     * This method should be used to remove HtmlAttributeChangeListener that were registered
     * for all attributes of all HtmlElements contained in this page.
     *
     * @param listener the attribute change listener to be removed
     * @see #addHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    public void removeHtmlAttributeChangeListener(final HtmlAttributeChangeListener listener) {
        WebAssert.notNull("listener", listener);
        synchronized (lock_) {
            if (attributeListeners_ != null) {
                attributeListeners_.remove(listener);
            }
        }
    }

    /**
     * Notifies all registered listeners for the given event to add an attribute.
     * @param event the event to fire
     */
    void fireHtmlAttributeAdded(final HtmlAttributeChangeEvent event) {
        final List<HtmlAttributeChangeListener> listeners = safeGetAttributeListeners();
        if (listeners != null) {
            for (final HtmlAttributeChangeListener listener : listeners) {
                listener.attributeAdded(event);
            }
        }
    }

    /**
     * Notifies all registered listeners for the given event to replace an attribute.
     * @param event the event to fire
     */
    void fireHtmlAttributeReplaced(final HtmlAttributeChangeEvent event) {
        final List<HtmlAttributeChangeListener> listeners = safeGetAttributeListeners();
        if (listeners != null) {
            for (final HtmlAttributeChangeListener listener : listeners) {
                listener.attributeReplaced(event);
            }
        }
    }

    /**
     * Notifies all registered listeners for the given event to remove an attribute.
     * @param event the event to fire
     */
    void fireHtmlAttributeRemoved(final HtmlAttributeChangeEvent event) {
        final List<HtmlAttributeChangeListener> listeners = safeGetAttributeListeners();
        if (listeners != null) {
            for (final HtmlAttributeChangeListener listener : listeners) {
                listener.attributeRemoved(event);
            }
        }
    }

    private List<HtmlAttributeChangeListener> safeGetAttributeListeners() {
        synchronized (lock_) {
            if (attributeListeners_ != null) {
                return new ArrayList<HtmlAttributeChangeListener>(attributeListeners_);
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkChildHierarchy(final org.w3c.dom.Node newChild) throws DOMException {
        if (newChild instanceof Element) {
            if (getDocumentElement() != null) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    "The Document may only have a single child Element.");
            }
        }
        else if (newChild instanceof DocumentType) {
            if (getDoctype() != null) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    "The Document may only have a single child DocumentType.");
            }
        }
        else if (!((newChild instanceof Comment) || (newChild instanceof ProcessingInstruction))) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                "The Document may not have a child of this type: " + newChild.getNodeType());
        }
        super.checkChildHierarchy(newChild);
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * @return true if the OnbeforeunloadHandler has accepted to change the page
     */
    public boolean isOnbeforeunloadAccepted() {
        return executeEventHandlersIfNeeded(Event.TYPE_BEFORE_UNLOAD);
    }

    /**
     * Returns <tt>true</tt> if an HTML parser is operating on this page, adding content to it.
     * @return <tt>true</tt> if an HTML parser is operating on this page, adding content to it
     */
    public boolean isBeingParsed() {
        return parserCount_ > 0;
    }

    /**
     * Called by the HTML parser to let the page know that it has started parsing some content for this page.
     */
    void registerParsingStart() {
        parserCount_++;
    }

    /**
     * Called by the HTML parser to let the page know that it has finished parsing some content for this page.
     */
    void registerParsingEnd() {
        parserCount_--;
    }

    /**
     * Returns <tt>true</tt> if an HTML parser is operating on some HTML
     * snippet for adding content to some element on this page.
     *
     * @return <tt>true</tt> if an HTML parser is operating on html snippet
     *         for adding content to to some element on this page
     */
    boolean isParsingHtmlSnippet() {
        return snippetParserCount_ > 0;
    }

    /**
     * Called by the HTML parser to let the page know that it has started
     * parsing HTML snippet for innerHTML or outerHTML of some element on
     * this page.
     */
    void registerSnippetParsingStart() {
        snippetParserCount_++;
    }

    /**
     * Called by the HTML parser to let the page know that it has finished
     * parsing HTML snippet for innerHTML or outerHTML of some element on
     * this page.
     */
    void registerSnippetParsingEnd() {
        snippetParserCount_--;
    }

    /**
     * Refreshes the page by sending the same parameters as previously sent to get this page.
     * @return the newly loaded page.
     * @throws IOException if an IO problem occurs
     */
    public Page refresh() throws IOException {
        return getWebClient().getPage(getWebResponse().getRequestSettings());
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * <p>
     * Parses the given string as would it belong to the content being parsed
     * at the current parsing position
     * </p>
     * @param string the HTML code to write in place
     */
    public void writeInParsedStream(final String string) {
        builder_.pushInputString(string);
    }

    /**
     * Sets the builder to allow page to send content from document.write(ln) calls.
     * @param htmlUnitDOMBuilder the builder
     */
    void setBuilder(final HtmlUnitDOMBuilder htmlUnitDOMBuilder) {
        builder_ = htmlUnitDOMBuilder;
    }

    /**
     * Returns the current builder.
     * @return the current builder
     */
    HtmlUnitDOMBuilder getBuilder() {
        return builder_;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * @return the current selection
     */
    public Range getSelection() {
        return selection_;
    }

    /**
     * Returns all namespaces defined in the root element of this page.
     * <p> The default namespace has a key of an empty string.
     * @return namespaces
     */
    public Map<String, String> getNamespaces() {
        final org.w3c.dom.NamedNodeMap attributes = getDocumentElement().getAttributes();
        final Map<String, String> namespaces = new HashMap<String, String>();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Attr attr = (Attr) attributes.item(i);
            String name = attr.getName();
            if (name.startsWith("xmlns")) {
                name = name.substring(5);
                if (name.startsWith(":")) {
                    name = name.substring(1);
                }
                namespaces.put(name, attr.getValue());
            }
        }
        return namespaces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDocumentType(final DomDocumentType type) {
        super.setDocumentType(type);
    }
}

/**
 * TODO: promote it for a larger usage
 */
class SimpleRange implements Range, Serializable {

    private static final long serialVersionUID = 5779974839466976193L;

    private org.w3c.dom.Node startContainer_, endContainer_;
    private int startOffset_, endOffset_;

    SimpleRange(final DomElement documentElement) {
        startContainer_ = documentElement;
        endContainer_ = documentElement;
        startOffset_ = 0;
        endOffset_ = 0;
    }

    public DocumentFragment cloneContents() throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public Range cloneRange() throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void collapse(final boolean toStart) throws DOMException {
        if (toStart) {
            endContainer_ = startContainer_;
            endOffset_ = startOffset_;
        }
        else {
            startContainer_ = endContainer_;
            startOffset_ = endOffset_;
        }
    }

    public short compareBoundaryPoints(final short how, final Range sourceRange) throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void deleteContents() throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void detach() throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public DocumentFragment extractContents() throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public boolean getCollapsed() throws DOMException {
        return startContainer_ == endContainer_ && startOffset_ == endOffset_;
    }

    public org.w3c.dom.Node getCommonAncestorContainer() throws DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public org.w3c.dom.Node getEndContainer() throws DOMException {
        return endContainer_;
    }

    public int getEndOffset() throws DOMException {
        return endOffset_;
    }

    public org.w3c.dom.Node getStartContainer() throws DOMException {
        return startContainer_;
    }

    public int getStartOffset() throws DOMException {
        return startOffset_;
    }

    public void insertNode(final org.w3c.dom.Node newNode) throws DOMException, RangeException {
        throw new RuntimeException("Not implemented!");
    }

    public void selectNode(final org.w3c.dom.Node refNode) throws RangeException, DOMException {
        startContainer_ = refNode;
        startOffset_ = 0;
        endContainer_ = refNode;
        endOffset_ = refNode.getTextContent().length();
    }

    public void selectNodeContents(final org.w3c.dom.Node refNode) throws RangeException, DOMException {
        startContainer_ = refNode.getFirstChild();
        startOffset_ = 0;
        endContainer_ = refNode.getLastChild();
        endOffset_ = refNode.getLastChild().getTextContent().length();
    }

    public void setEnd(final org.w3c.dom.Node refNode, final int offset) throws RangeException, DOMException {
        endContainer_ = refNode;
        endOffset_ = offset;
    }

    public void setEndAfter(final org.w3c.dom.Node refNode) throws RangeException, DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void setEndBefore(final org.w3c.dom.Node refNode) throws RangeException, DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void setStart(final org.w3c.dom.Node refNode, final int offset) throws RangeException, DOMException {
        startContainer_ = refNode;
        startOffset_ = offset;
    }

    public void setStartAfter(final org.w3c.dom.Node refNode) throws RangeException, DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void setStartBefore(final org.w3c.dom.Node refNode) throws RangeException, DOMException {
        throw new RuntimeException("Not implemented!");
    }

    public void surroundContents(final org.w3c.dom.Node newParent) throws DOMException, RangeException {
        throw new RuntimeException("Not implemented!");
    }
}
