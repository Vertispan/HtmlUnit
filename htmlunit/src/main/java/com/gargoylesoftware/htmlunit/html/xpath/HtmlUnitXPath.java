/*
 * Copyright (c) 2002-2008 Gargoyle Software Inc. All rights reserved.
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
package com.gargoylesoftware.htmlunit.html.xpath;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xml.utils.DefaultErrorHandler;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.compiler.XPathParser;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;

import com.gargoylesoftware.htmlunit.html.DomNode;

/**
 * XPath adapter implementation for HtmlUnit.
 *
 * @version $Revision$
 * @author Ahmed Ashour
 */
public class HtmlUnitXPath {

    /**
     * For backward compatibility.
     * @deprecated
     */
    private String xpathExpr_;
    private Expression mainExp_;

    private transient FunctionTable funcTable_;
    
    /** Represents a select type expression. */
    public static final int SELECT = 0;

    /** Represents a match type expression. */
    public static final int MATCH = 1;

    /**
     * Initiates the function table.
     */
    private void initFunctionTable() {
        funcTable_ = new FunctionTable();
        funcTable_.installFunction("lower-case", LowerCaseFunction.class);
    }

    /**
     * Construct given an XPath expression string.
     * @param xpathExpr the XPath expression
     * @deprecated As of 2.0, please use {@link DomNode#getByXPath(String)} instead.
     */
    public HtmlUnitXPath(final String xpathExpr) {
        this.xpathExpr_ = xpathExpr;
    }

    /**
     * Constructor.
     * @param exprString the XPath expression
     * @param locator the location of the expression, may be <tt>null</tt>
     * @param prefixResolver a prefix resolver to use to resolve prefixes to namespace URIs
     * @param type one of {@link #SELECT} or {@link #MATCH}
     * @param errorListener the error listener, or <tt>null</tt> if default should be used
     * @throws TransformerException if a syntax or other error occurs
     */
    public HtmlUnitXPath(String exprString, final SourceLocator locator, final PrefixResolver prefixResolver,
            final int type, ErrorListener errorListener) throws TransformerException {
        initFunctionTable();
        if (errorListener == null) {
            errorListener = new DefaultErrorHandler();
        }
        exprString = preProcessXPath(exprString);

        final XPathParser parser = new XPathParser(errorListener, locator);
        final Compiler compiler = new Compiler(errorListener, locator, funcTable_);

        if (SELECT == type) {
            parser.initXPath(compiler, exprString, prefixResolver);
        }
        else if (MATCH == type) {
            parser.initMatchPattern(compiler, exprString, prefixResolver);
        }
        else {
            throw new RuntimeException(XSLMessages.createXPATHMessage(
                XPATHErrorResources.ER_CANNOT_DEAL_XPATH_TYPE, new Object[]{Integer.toString(type)}));
        }

        final Expression expr = compiler.compile(0);

        mainExp_ = expr;

        if (locator != null && locator instanceof ExpressionNode) {
            expr.exprSetParent((ExpressionNode) locator);
        }
    }

    /**
     * Processes the XPath string before passing it to the engine.
     * The current implementation lower-case the attribute name.
     */
    private String preProcessXPath(String string) {
        //Not a very clean way
        final Pattern pattern = Pattern.compile("(@[a-zA-Z]+)");
        final Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            final String attribute = matcher.group(1);
            string = string.replace(attribute, attribute.toLowerCase());
        }
        return string;
    }

    /**
     * Given an expression and a context, evaluate the XPath and return the result.
     *
     * @param xpathContext the execution context
     * @param contextNode the node that "." expresses
     * @param namespaceContext the context in which namespaces in the XPath are supposed to be expanded
     * @return the result of the XPath or null if callbacks are used
     * @throws TransformerException if the error condition is severe enough to halt processing
     */
    public XObject execute(final XPathContext xpathContext, final int contextNode,
        final PrefixResolver namespaceContext) throws TransformerException {
        xpathContext.pushNamespaceContext(namespaceContext);

        xpathContext.pushCurrentNodeAndExpression(contextNode, contextNode);

        XObject xobj = null;

        try {
            xobj = mainExp_.execute(xpathContext);
        }
        catch (final TransformerException te) {
            te.setLocator(mainExp_);
            final ErrorListener el = xpathContext.getErrorListener();
            if (null != el) {
                el.error(te);
            }
            else {
                throw te;
            }
        }
        catch (Exception e) {
            while (e instanceof WrappedRuntimeException) {
                e = ((WrappedRuntimeException) e).getException();
            }
            String msg = e.getMessage();

            if (msg == null || msg.length() == 0) {
                msg = XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_XPATH_ERROR, null);

            }
            final TransformerException te = new TransformerException(msg, mainExp_, e);
            final ErrorListener el = xpathContext.getErrorListener();
            if (null != el) {
                el.fatalError(te);
            }
            else {
                throw te;
            }
        }
        finally {
            xpathContext.popNamespaceContext();
            xpathContext.popCurrentNodeAndExpression();
        }

        return xobj;
    }

    /**
     * Select only the first node selected by this XPath expression.
     * If multiple nodes match, only one node will be returned.
     * The selected node will be the first selected node in document-order, as defined by the XPath specification.
     * @param node the node, node-set or Context object for evaluation.
     * @return the first node selected by this XPath expression
     * @deprecated As of 2.0, please use {@link DomNode#getByXPath(String)} instead.
     */
    public Object selectSingleNode(final Object node) {
        if (!(node instanceof DomNode)) {
            throw new IllegalArgumentException("" + node + " must be DomNode.");
        }
        return ((DomNode) node).getFirstByXPath(xpathExpr_);
    }

    /**
     * Select all nodes that are selected by this XPath expression.
     * If multiple nodes match, multiple nodes will be returned. Nodes will be returned in document-order,
     * as defined by the XPath specification. If the expression selects a non-node-set
     * (i.e. a number, boolean, or string) then a List containing just that one object is returned.
     * @param node the node, node-set or Context object for evaluation.
     * @return the node-set of all items selected by this XPath expression
     * @deprecated As of 2.0, please use {@link DomNode#getByXPath(String)} instead.
     */
    public List< ? extends Object> selectNodes(final Object node) {
        if (!(node instanceof DomNode)) {
            throw new IllegalArgumentException("" + node + " must be DomNode.");
        }
        return ((DomNode) node).getByXPath(xpathExpr_);
    }

    /**
     * Retrieves the string-value of the result of evaluating this XPath expression when evaluated against
     * the specified context.
     * The string-value of the expression is determined per the string(..) core function defined in
     * the XPath specification. This means that an expression that selects zero nodes will return the empty string,
     * while an expression that selects one-or-more nodes will return the string-value of the first node.
     * @param node the node, node-set or Context object for evaluation.
     * @return the string-value of the result of evaluating this expression with the specified context node
     * @deprecated As of 2.0, please use {@link DomNode#getByXPath(String)} instead.
     */
    public String stringValueOf(final Object node) {
        if (!(node instanceof DomNode)) {
            throw new IllegalArgumentException("" + node + " must be DomNode.");
        }
        return (String) ((DomNode) node).getFirstByXPath("string(" + xpathExpr_ + ')');
    }
}
