/*
 * Copyright (c) 2002, 2003 Gargoyle Software Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment:
 *
 *       "This product includes software developed by Gargoyle Software Inc.
 *        (http://www.GargoyleSoftware.com/)."
 *
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 4. The name "Gargoyle Software" must not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *    For written permission, please contact info@GargoyleSoftware.com.
 * 5. Products derived from this software may not be called "HtmlUnit", nor may
 *    "HtmlUnit" appear in their name, without prior written permission of
 *    Gargoyle Software Inc.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GARGOYLE
 * SOFTWARE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.html.dom.HTMLDocumentImpl;
import org.mozilla.javascript.Scriptable;

/**
 * The javascript object "HTMLElement" which is the base class for all html
 * objects.  This will typically wrap an instance of {@link HtmlElement}.
 *
 * @version  $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author Barnaby Court
 */
public class HTMLElement extends SimpleScriptable {
    private Style style_;

     /**
      * Create an instance.
      */
     public HTMLElement() {
     }


    /**
     * Javascript constructor.  This must be declared in every javascript file because
     * the rhino engine won't walk up the hierarchy looking for constructors.
     */
    public void jsConstructor() {
    }


    /**
     * Return the style object for this element.
     *
     * @return The style object
     */
    public Object jsGet_style() {
        getLog().debug("HTMLElement.jsGet_Style() style=["+style_+"]");
        return style_;
    }


     /**
      * Set the html element that corresponds to this javascript object
      * @param htmlElement The html element
      */
     public void setHtmlElement( final HtmlElement htmlElement ) {
         super.setHtmlElement(htmlElement);

         style_ = (Style)makeJavaScriptObject("Style");
         style_.initialize(this);
     }


    /**
     * Return true if this element is disabled.
     * @return True if this element is disabled.
     */
    public boolean jsGet_disabled() {
        getLog().warn(
            "Getting the disabled attribute for non-submittable elements"
            + " is not allowed according to the HTML specification.  Be aware"
            + " that you are using a non-portable feature");
        return getHtmlElementOrDie().isAttributeDefined("disabled");
    }


    /**
     * Set whether or not to disable this element
     * @param disabled True if this is to be disabled.
     */
    public void jsSet_disabled( final boolean disabled ) {
        getLog().warn(
            "Setting the disabled attribute for non-submittable elements"
            + " is not allowed according to the HTML specification.  Be aware"
            + " that you are using a non-portable feature");
        final Element xmlElement = getHtmlElementOrDie().getElement();
        if( disabled ) {
            xmlElement.setAttribute("disabled", "disabled");
        }
        else {
            xmlElement.removeAttribute("disabled");
        }
    }


    /**
     * Return the tag name of this element.
     * @return The tag name
     */
    public String jsGet_tagName() {
        return getHtmlElementOrDie().getTagName().toUpperCase();
    }


    /**
     * Return the value of the named attribute.
     * @param name The name of the variable
     * @param start The scriptable to get the variable from.
     * @return The attribute value
     */
    public Object get( String name, Scriptable start ) {
        Object result = super.get( name, start );
        if ( result == NOT_FOUND ) {
            final HtmlElement htmlElement = getHtmlElementOrNull();
            if( htmlElement != null) {
                final String value = htmlElement.getAttributeValue(name);
                if( value.length() != 0 ) {
                    result = value;
                }
            }
        }
        return result;
    }


    /**
     * Add an HTML element to the element
     * @param childObject The element to add to this element
     * @return The newly added child element.
     */
    public Object jsFunction_appendChild(final Object childObject) {
        final Object appendedChild;
        if (childObject instanceof HTMLElement) {
            // Get XML element for the HTML element passed in
            final HtmlElement childHtmlElement = ((HTMLElement) childObject).getHtmlElementOrDie();
            final Element childXmlNode = childHtmlElement.getElement();

            // Get the parent XML element that the child should be added to.
            final HtmlElement parentElement = this.getHtmlElementOrDie();
            final Element parentXmlNode = parentElement.getElement();

            // Append the child to the parent element
            if ( parentXmlNode.appendChild(childXmlNode) == null ) {
                appendedChild = null;
            }
            else {
                appendedChild = childObject;
            }
        }
        else {
            appendedChild = null;
        }
        return appendedChild;
    }


    /**
     * Add an HTML element as a child to this element before the referenced
     * element.  If the referenced element is null, append to the end.
     * @param newChildObject The element to add to this element
     * @param refChildObject The element before which to add the new child
     * @return The newly added child element.
     */
    public Object jsFunction_insertBefore(final Object newChildObject,
        final Object refChildObject) {
        final Object appendedChild;
        if (newChildObject instanceof HTMLElement &&
            refChildObject instanceof HTMLElement) {
            // Get XML elements for the HTML elements passed in
            final HtmlElement newChildHtmlElement = ((HTMLElement) newChildObject).getHtmlElementOrDie();
            final Element newChildXmlNode = newChildHtmlElement.getElement();
            Element refChildXmlNode = null;
            if (refChildObject != null) {
                final HtmlElement refChildHtmlElement = ((HTMLElement) refChildObject).getHtmlElementOrDie();
                refChildXmlNode = refChildHtmlElement.getElement();
            }

            // Get the parent XML element that the child should be added to.
            final HtmlElement parentElement = this.getHtmlElementOrDie();
            final Element parentXmlNode = parentElement.getElement();

            // Append the child to the parent element
            if ( parentXmlNode.insertBefore(newChildXmlNode,
                refChildXmlNode) == null ) {
                appendedChild = null;
            }
            else {
                appendedChild = newChildObject;
            }
        }
        else {
            appendedChild = null;
        }
        return appendedChild;
    }


    /**
     * Remove an HTML element from this element
     * @param childObject The element to remove from this element
     * @return The removed child element.
     */
    public Object jsFunction_removeChild(final Object childObject) {
        final Object removedChild;
        if (childObject instanceof HTMLElement) {
            // Get XML element for the HTML element passed in
            final HtmlElement childHtmlElement = ((HTMLElement) childObject).getHtmlElementOrDie();
            final Element childXmlNode = childHtmlElement.getElement();

            // Get the parent XML element that the child should be added to.
            final HtmlElement parentElement = this.getHtmlElementOrDie();
            final Element parentXmlNode = parentElement.getElement();

            // Remove the child from the parent element
            if ( parentXmlNode.removeChild(childXmlNode) == null ) {
                removedChild = null;
            }
            else {
                removedChild = childObject;
                parentElement.getPage().removeHtmlElement(childXmlNode);
            }
        }
        else {
            removedChild = null;
        }
        return removedChild;
    }


    /**
     * Get the JavaScript property "parentNode" for the node that
     * contains the current node.
     * @return The parent node
     */
    public Object jsGet_parentNode() {
        final HtmlElement htmlElement = getHtmlElementOrDie();
        final Element xmlElement = htmlElement.getElement();
        final Node parentXmlNode = xmlElement.getParentNode();
        return getJavaScriptElementFromXmlNode(parentXmlNode,
            htmlElement.getPage());
    }


    /**
     * Get the JavaScript property "nextSibling" for the node that
     * contains the current node.
     * @return The next sibling node or null if the current node has
     * no next sibling.
     */
    public Object jsGet_nextSibling() {
        final HtmlElement htmlElement = getHtmlElementOrDie();
        final Element xmlElement = htmlElement.getElement();
        final Node siblingXmlNode = xmlElement.getNextSibling();
        return getJavaScriptElementFromXmlNode(siblingXmlNode,
            htmlElement.getPage());
    }


    /**
     * Get the JavaScript property "previousSibling" for the node that
     * contains the current node.
     * @return The previous sibling node or null if the current node has
     * no previous sibling.
     */
    public Object jsGet_previousSibling() {
        final HtmlElement htmlElement = getHtmlElementOrDie();
        final Element xmlElement = htmlElement.getElement();
        final Node siblingXmlNode = xmlElement.getPreviousSibling();
        return getJavaScriptElementFromXmlNode(siblingXmlNode,
            htmlElement.getPage());
    }


    /**
     * Get the JavaScript property "firstChild" for the node that
     * contains the current node.
     * @return The first child node or null if the current node has
     * no children.
     */
    public Object jsGet_firstChild() {
        final HtmlElement htmlElement = getHtmlElementOrDie();
        final Element xmlElement = htmlElement.getElement();
        final Node childXmlNode = xmlElement.getFirstChild();
        return getJavaScriptElementFromXmlNode(childXmlNode,
            htmlElement.getPage());
    }


    /**
     * Get the JavaScript property "lastChild" for the node that
     * contains the current node.
     * @return The last child node or null if the current node has
     * no children.
     */
    public Object jsGet_lastChild() {
        final HtmlElement htmlElement = getHtmlElementOrDie();
        final Element xmlElement = htmlElement.getElement();
        final Node childXmlNode = xmlElement.getLastChild();
        return getJavaScriptElementFromXmlNode(childXmlNode,
            htmlElement.getPage());
    }


    /**
     * Get the JavaScript element corresponding to an XML node.
     * @param xmlNode The XML node to search for.
     * @param page The HTML document to search in.
     * @return The JavaScript element.
     */
    protected Object getJavaScriptElementFromXmlNode(Node xmlNode,
        HtmlPage page) {
        if ( xmlNode == null ) {
            return null;
        }
        if ( ( xmlNode instanceof Element ) == false ) {
            if( xmlNode instanceof HTMLDocumentImpl == false ) {
                throw new IllegalStateException(
                    "XML node is not an Element.  Only Elements are currently supported.  Node class: "
                    + xmlNode.getClass() );
            }
            return null;
        }
        final Element xmlElement = (Element) xmlNode;
        final HtmlElement htmlElement = page.getHtmlElement( xmlElement );
        final Object jsElement = getScriptableFor( htmlElement );
        return jsElement;
    }
}
