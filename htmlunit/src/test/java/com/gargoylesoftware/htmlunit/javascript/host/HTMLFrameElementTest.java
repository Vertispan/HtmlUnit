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
package com.gargoylesoftware.htmlunit.javascript.host;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebTestCase;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests for {@link HTMLFrameElement} when used for {@link HtmlFrame}.
 *
 * @version $Revision$
 * @author Chris Erskine
 * @author Marc Guillemot
 * @author Thomas Robbs
 * @author David K. Taylor
 * @author Ahmed Ashour
 */
public class HTMLFrameElementTest extends WebTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testFrameName() throws Exception {
        final String html
            = "<html><head><title>first</title></head>\n"
            + "<frameset cols='20%,80%'>\n"
            + "    <frame id='frame1'>\n"
            + "    <frame name='Frame2' onload='alert(this.name)' id='frame2'>\n"
            + "</frameset></html>";
        final String[] expectedAlerts = {"Frame2"};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(html, collectedAlerts);

        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Regression test for
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1101525&group_id=47038&atid=448266
     * @throws Exception if the test fails
     */
    @Test
    public void testLocation() throws Exception {
        testLocation("Frame1.location = \"frame.html\"");
        testLocation("Frame1.location.replace(\"frame.html\")");
    }

    private void testLocation(final String jsExpr) throws Exception {
        final WebClient webClient = new WebClient();
        final MockWebConnection webConnection =
            new MockWebConnection(webClient);
        final List<String> collectedAlerts = new ArrayList<String>();

        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final String firstContent
            = "<html><head><title>first</title></head>\n"
            + "<frameset cols='*' onload='" + jsExpr + "'>\n"
            + "    <frame name='Frame1' src='subdir/frame.html'>\n"
            + "</frameset></html>";
        final String defaultContent
            = "<html><head><script>alert(location)</script></head></html>";

        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setDefaultResponse(defaultContent);

        final String[] expectedAlerts = {URL_FIRST + "/subdir/frame.html", URL_FIRST + "/frame.html"};

        webClient.setWebConnection(webConnection);

        final HtmlPage page = (HtmlPage) webClient.getPage(URL_FIRST);
        assertEquals("first", page.getTitleText());

        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testContentDocument() throws Exception {
        final String content
            = "<html><head><title>first</title>\n"
                + "<script>\n"
                + "function test()\n"
                + "{\n"
                + "  alert(document.getElementById('myFrame').contentDocument == frames.foo.document);\n"
                + "}\n"
                + "</script></head>\n"
                + "<frameset rows='*' onload='test()'>\n"
                + "<frame name='foo' id='myFrame' src='about:blank'/>\n"
                + "</frameset>\n"
                + "</html>";
        final String[] expectedAlerts = {"true"};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(BrowserVersion.FIREFOX_2, content, collectedAlerts);

        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testContentWindow() throws Exception {
        final String content
            = "<html><head><title>first</title>\n"
                + "<script>\n"
                + "function test()\n"
                + "{\n"
                + "  alert(document.getElementById('myFrame').contentWindow == frames.foo);\n"
                + "}\n"
                + "</script></head>\n"
                + "<frameset rows='*' onload='test()'>\n"
                + "<frame name='foo' id='myFrame' src='about:blank'/>\n"
                + "</frameset>\n"
                + "</html>";
        final String[] expectedAlerts = {"true"};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(content, collectedAlerts);

        assertEquals(expectedAlerts, collectedAlerts);
    }
    
    /**
     * Regression test for bug 1236048
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1236048&group_id=47038&atid=448266
     * @throws Exception if the test fails
     */
    @Test
    public void testWriteFrameset() throws Exception {
        final String content1 = "<html><head>\n"
            + "<script>\n"
            + "    document.write('<frameset>');\n"
            + "    document.write('<frame src=\"page2.html\" name=\"leftFrame\">');\n"
            + "    document.write('</frameset>');\n"
            + "</script>\n"
            + "</head></html>";
        final String content2 = "<html><head><script>alert(2)</script></head></html>";

        final WebClient webClient = new WebClient();
        final MockWebConnection webConnection = new MockWebConnection(webClient);
        webClient.setWebConnection(webConnection);

        webConnection.setDefaultResponse(content2);
        webConnection.setResponse(URL_FIRST, content1);

        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final String[] expectedAlerts = {"2"};

        webClient.getPage(URL_FIRST);

        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Regression test fo Bug #1289060
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1289060&group_id=47038&atid=448266
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testFrameLoadedAfterParent() throws Exception {
        final WebClient webClient = new WebClient();
        final MockWebConnection webConnection = new MockWebConnection(webClient);

        final String mainContent
            = "<html><head><title>first</title></head><body>\n"
            + "<iframe name='testFrame' src='testFrame.html'></iframe>\n"
            + "<div id='aButton'>test text</div>\n"
            + "</body></html>";
        final String frameContent
            = "<html><head></head><body>\n"
            + "<script>\n"
            + "alert(top.document.getElementById('aButton').tagName);\n"
            + "</script>\n"
            + "</body></html>";

        webConnection.setResponse(URL_GARGOYLE, mainContent);
        webConnection.setResponse(new URL(URL_GARGOYLE + "testFrame.html"), frameContent);

        webClient.setWebConnection(webConnection);
        final List<String> collectedAlerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final String[] expectedAlerts = {"DIV"};

        webClient.getPage(URL_GARGOYLE);
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Regression test for bug 1192854.
     * @throws Exception if the test fails
     */
    @Test
    public void testFrameTag1192854() throws Exception {
        final String htmlContent
            = "<html>\n"
            + "<script>\n"
            + "var root=this;\n"
            + "function listframes(frame) {\n"
            + "  if (frame == null) {\n"
            + "    alert('frame=null');\n"
            + "  } else {\n"
            + "    alert('frame=OK');\n"
            + "    var len = frame.frames.length;\n"
            + "    alert('frames.length=' + len);\n"
            + "    for (var i=0; i<len; i++) {\n"
            + "      listframes(frame.frames[i]);\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "document.write('<frameset id=\"frameset1\" "
            + "rows=\"50,50\"><frame id=\"frame1-1\" "
            + "src=\"about:blank\"><frame id=\"frame1-2\" "
            + "src=\"about:blank\"></frameset>');\n"
            + "listframes(root);\n"
            + "</script>\n"
            + "</html>";

        final String[] expectedAlerts = {"frame=OK",
            "frames.length=2",
            "frame=OK",
            "frames.length=0",
            "frame=OK",
            "frames.length=0"};

        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(htmlContent, collectedAlerts);

        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception If the test fails.
     */
    @Test
    public void testOnloadNull() throws Exception {
        final String html =
            "<html><head>\n"
            + "<script>\n"
            + "  function handler() {}"
            + "  function test() {\n"
            + "    var iframe = document.getElementById('myFrame');\n"
            + "    iframe.onload = handler;\n"
            + "    alert(iframe.onload);\n"
            + "    iframe.onload = null;\n"
            + "    alert(iframe.onload);\n"
            + "  }\n"
            + "</script>\n"
            + "<body onload=test()>\n"
            + "  <iframe id='myFrame'></iframe>\n"
            + "</body></html>";
        final String[] expectedAlerts = {"\nfunction handler() {\n}\n", "null"};
        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(html, collectedAlerts);
        assertEquals(expectedAlerts, collectedAlerts);
    }
}
