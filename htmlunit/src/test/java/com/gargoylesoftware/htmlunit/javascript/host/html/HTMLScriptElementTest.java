/*
 * Copyright (c) 2002-2017 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.javascript.host.html;

import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;

/**
 * Unit tests for {@link HTMLScriptElement}.
 * TODO: check event order with defer in real browser WITHOUT using alert(...) as it impacts ordering.
 * Some expectations seems to be incorrect.
 * @author Daniel Gredler
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HTMLScriptElementTest extends WebDriverTestCase {

    /**
     * Verifies that the <tt>onreadystatechange</tt> handler is invoked correctly.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("1 2 3 4 onload ")
    public void onReadyStateChangeHandler() throws Exception {
        final String html = "<html>\n"
            + "  <head>\n"
            + "    <title>test</title>\n"
            + "    <script>\n"
            + "      function test() {\n"
            + "        var script = document.createElement('script');\n"
            + "        script.id = 'b';\n"
            + "        script.type = 'text/javascript';\n"
            + "        script.onreadystatechange = null;\n"
            + "        script.onreadystatechange = function() {\n"
            + "          document.getElementById('myTextarea').value += script.id + '=' + script.readyState + ' ';\n"
            + "          if (this.readyState == 'loaded') {\n"
            + "            alert(document.getElementById('myTextarea').value);\n"
            + "          }\n"
            + "        }\n"
            + "        script.onload = function() {\n"
            + "          document.getElementById('myTextarea').value += 'onload ';\n"
            + "          alert(document.getElementById('myTextarea').value);\n"
            + "        }\n"
            + "        document.getElementById('myTextarea').value += '1 ';\n"
            + "        script.src = 'script.js';\n"
            + "        document.getElementById('myTextarea').value += '2 ';\n"
            + "        document.getElementsByTagName('head')[0].appendChild(script);\n"
            + "        document.getElementById('myTextarea').value += '3 ';\n"
            + "      }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "  <body onload='test()'>\n"
            + "    <textarea id='myTextarea' cols='40'></textarea>\n"
            + "  </body></html>";

        final String js = "document.getElementById('myTextarea').value += '4 ';";

        getMockWebConnection().setDefaultResponse(js, JAVASCRIPT_MIME_TYPE);
        loadPageWithAlerts2(html);
    }

    /**
     * Test for bug https://sourceforge.net/tracker/?func=detail&atid=448266&aid=1782719&group_id=47038.
     * @throws Exception if the test fails
     */
    @Test
    public void srcWithJavaScriptProtocol_Static() throws Exception {
        final String html = "<html><head><script src='javascript:\"alert(1)\"'></script></head><body></body></html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"§§URL§§foo.js", "foo.js", "§§URL§§", ""})
    public void srcPropertyShouldBeAFullUrl() throws Exception {
        final String html =
                "<html>\n"
                + "<head>\n"
                + "  <title>foo</title>\n"
                + "  <script>\n"
                + "    function test() {\n"
                + "      var script = document.getElementById('my');\n"
                + "      alert(script.src);\n"
                + "      alert(script.getAttribute('src'));\n"

                + "      var script2 = document.getElementById('my2');\n"
                + "      alert(script2.src);\n"
                + "      alert(script2.getAttribute('src'));\n"
                + "    }\n"
                + "  </script>\n"
                + "</head>\n"
                + "<body onload='test()'>\n"
                + "  <script id='my' src='foo.js'></script>\n"
                + "  <script id='my2' src=''></script>\n"
                + "</body></html>";

        getMockWebConnection().setDefaultResponse("", "text/javascript");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"", "null", "", "null"})
    public void srcPropertyNoSource() throws Exception {
        final String html =
                "<html>\n"
                + "<head>\n"
                + "  <title>foo</title>\n"
                + "  <script>\n"
                + "    function test() {\n"
                + "      var script = document.getElementById('my');\n"
                + "      alert(script.src);\n"
                + "      alert(script.getAttribute('src'));\n"

                + "      var script2 = document.createElement('script');\n"
                + "      alert(script2.src);\n"
                + "      alert(script2.getAttribute('src'));\n"
                + "    }\n"
                + "  </script>\n"
                + "</head>\n"
                + "<body onload='test()'>\n"
                + "  <script id='my'></script>\n"
                + "</body></html>";

        getMockWebConnection().setDefaultResponse("", "text/javascript");

        loadPageWithAlerts2(html);
    }

    /**
     * Test for bug https://sourceforge.net/tracker/?func=detail&atid=448266&aid=1782719&group_id=47038.
     * @throws Exception if the test fails
     */
    @Test
    public void srcWithJavaScriptProtocol_Dynamic() throws Exception {
        final String html =
              "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var script=document.createElement('script');\n"
            + "    script.src=\"javascript: 'alert(1)'\";\n"
            + "    document.getElementsByTagName('head')[0].appendChild(script);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test for bug 2993940.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void reexecuteModifiedScript() throws Exception {
        final String html =
              "<html><head><title>foo</title></head><body>\n"
            + "<script>\n"
            + "  alert('start');\n"
            + "  var script = document.getElementsByTagName('script')[0];\n"
            + "  script.text = \"alert('executed');\";\n"
            + "  alert('end');\n"
            + "</script>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Creates a new script element and adds the source using <code>createTextNode</code> and <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void createElementWithCreateTextNode() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.createElement('script');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {alert('exception'); }\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Creates a new script element and adds the source using <code>createTextNode</code> and <code>appendChild</code>.
     * After that it appends the script element to the body.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "middle", "executed", "end"})
    public void createElementWithCreateTextNodeAndAppend() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.createElement('script');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {alert('exception'); }\n"
              + "  alert('middle');\n"
              + "  document.body.appendChild(script);\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Creates a new script element and adds the source using <code>.text</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void createElementWithSetText() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.createElement('script');\n"
              + "  script.text = \"alert('executed');\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Creates a new script element and adds the source using <code>.text</code>.
     * After that it appends the script element to the body.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "middle", "executed", "end"})
    public void createElementWithSetTextAndAppend() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.createElement('script');\n"
              + "  script.text = \"alert('executed');\";\n"
              + "  alert('middle');\n"
              + "  document.body.appendChild(script);\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Creates a new script element and adds the source using <code>.src</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void createElementWithSetSrc() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.createElement('script');\n"
              + "  script.src = \"" + URL_SECOND + "\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        final String js = "alert('executed');";
        getMockWebConnection().setResponse(URL_SECOND, js);

        loadPageWithAlerts2(html);
    }

    /**
     * Creates a new script element and adds the source using <code>.src</code>.
     * After that it appends the script element to the body.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "middle", "end", "executed"})
    public void createElementWithSetSrcAndAppend() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.createElement('script');\n"
              + "  script.src = \"" + URL_SECOND + "\";\n"
              + "  alert('middle');\n"
              + "  document.body.appendChild(script);\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        final String js = "alert('executed');";
        getMockWebConnection().setResponse(URL_SECOND, js);

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source of the current script element using <code>createTextNode</code> and <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void replaceSelfWithCreateTextNode() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementsByTagName('script')[0];\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {alert('exception'); }\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source of the current script element using <code>.text</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void replaceSelfWithSetText() throws Exception {
        // TODO this test is the same as #reexecuteModifiedScriptWhenReappending()
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementsByTagName('script')[0];\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  script.text = \"alert('executed');\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source of the current script element using <code>.src</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void replaceSelfWithSetSrc() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementsByTagName('script')[0];\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  script.src = \"" + URL_SECOND + "\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        final String js = "alert('executed');";
        getMockWebConnection().setResponse(URL_SECOND, js);

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the empty source of another script element using <code>createTextNode</code> and <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "executed", "end"})
    public void replaceWithCreateTextNodeEmpty() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'></script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {alert('exception'); }\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source containing just a blank of another script element using <code>createTextNode</code> and <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void replaceWithCreateTextNodeBlank() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'> </script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {alert('exception'); }\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source containing a script of another script element using <code>createTextNode</code> and <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"script", "start", "end"})
    public void replaceWithCreateTextNodeScript() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'>\n"
              + "  alert('script');\n"
              + "</script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {alert('exception'); }\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the empty source of another script element using <code>.text</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "executed", "end"})
    public void replaceWithSetTextEmpty() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'></script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  script.text = \"alert('executed');\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source containing just a blank of another script element using <code>.text</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void replaceWithSetTextBlank() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'> </script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  script.text = \"alert('executed');\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source containing a script of another script element using <code>.text</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"script", "start", "end"})
    public void replaceWithSetTextScript() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'>\n"
              + "  alert('script');\n"
              + "</script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  script.text = \"alert('executed');\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the empty source of another script element using <code>.src</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end", "executed"})
    public void replaceWithSetSrcEmpty() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'></script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  script.src = \"" + URL_SECOND + "\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        final String js = "alert('executed');";
        getMockWebConnection().setResponse(URL_SECOND, js);

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source containing just a blank of another script element using <code>.src</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"start", "end"})
    public void replaceWithSetSrcBlank() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'> </script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  script.src = \"" + URL_SECOND + "\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        final String js = "alert('executed');";
        getMockWebConnection().setResponse(URL_SECOND, js);

        loadPageWithAlerts2(html);
    }

    /**
     * Replaces the source containing a script of another script element using <code>.text</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"script", "start", "end"})
    public void replaceWithSetSrcScript() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script id='js1'>\n"
              + "  alert('script');\n"
              + "</script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  script.src = \"" + URL_SECOND + "\";\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</body></html>";

        final String js = "alert('executed');";
        getMockWebConnection().setResponse(URL_SECOND, js);

        loadPageWithAlerts2(html);
    }

    /**
     * Moves a script element from a div element to the body element using <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"executed", "start", "end"})
    public void moveWithAppend() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<div>\n"
              + "<script id='js1'>alert('executed');</script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  document.body.appendChild(script);\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</div>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Moves a script element from a div element to the body element using <code>insertBefore</code>.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"executed", "start", "end"})
    public void moveWithInsert() throws Exception {
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<div>\n"
              + "<script id='js1'>\n"
              + "  alert('executed');\n"
              + "</script>\n"
              + "<script>\n"
              + "  alert('start');\n"
              + "  var script = document.getElementById('js1');\n"
              + "  document.body.insertBefore(script, null);\n"
              + "  alert('end');\n"
              + "</script>\n"
              + "</div>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"script-for", "exception", "script-body"},
            IE = {"script-body", "script-for", "hello"})
    public void scriptForEvent() throws Exception {
        // IE accepts it with () or without
        scriptForEvent("onload");
        scriptForEvent("onload()");
    }

    private void scriptForEvent(final String eventName) throws Exception {
        final String html = "<html><head><title>foo</title>\n"
            + "<script FOR='window' EVENT='" + eventName + "' LANGUAGE='javascript'>\n"
            + "  alert('script-for');\n"
            + "  try {\n"
            + "    document.form1.txt.value='hello';\n"
            + "    alert(document.form1.txt.value);\n"
            + "  } catch(e) {alert('exception'); }\n"
            + "</script></head>\n"
            + "<body>\n"
            + "  <form name='form1'><input type='text' name='txt'></form>\n"
            + "  <script>\n"
            + "    alert('script-body');\n"
            + "  </script>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Verifies the correct the ordering of script element execution, deferred script element
     * execution, script ready state changes, deferred script ready state changes, and onload
     * handlers.
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({"3", "4", "2", "5"})
    public void onReadyStateChange_Order() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <title>test</title>\n"
            + "    <script defer=''>alert('3');</script>\n"
            + "    <script defer='' onreadystatechange='if(this.readyState==\"complete\") alert(\"6\");'>alert('4');</script>\n"
            + "    <script src='//:' onreadystatechange='if(this.readyState==\"complete\") alert(\"1\");'></script>\n"
            + "    <script defer='' src='//:' onreadystatechange='if(this.readyState==\"complete\") alert(\"7\");'></script>\n"
            + "    <script>alert('2')</script>\n"
            + "  </head>\n"
            + "  <body onload='alert(5)'></body>\n"
            + "</html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void onReadyStateChange_EventAvailable() throws Exception {
        final String html =
              "<html><body><script>\n"
            + "var s = document.createElement('script');\n"
            + "s.src = '//:';\n"
            + "s.onreadystatechange = function() {alert(window.event);};\n"
            + "document.body.appendChild(s);\n"
            + "</script></body></html>";
        loadPageWithAlerts2(html);
    }

    /**
     * Verifies the correct the ordering of script element execution, deferred script element
     * execution, script ready state changes, deferred script ready state changes, and onload
     * handlers when the document doesn't have an explicit <tt>body</tt> element.
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({"3", "4", "2"})
    public void onReadyStateChange_Order_NoBody() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <title>test</title>\n"
            + "    <script defer=''>alert('3');</script>\n"
            + "    <script defer='' onreadystatechange='if(this.readyState==\"complete\") alert(\"5\");'>alert('4');</script>\n"
            + "    <script src='//:' onreadystatechange='if(this.readyState==\"complete\") alert(\"1\");'></script>\n"
            + "    <script defer='' src='//:' onreadystatechange='if(this.readyState==\"complete\") alert(\"6\");'></script>\n"
            + "    <script>alert('2')</script>\n"
            + "  </head>\n"
            + "</html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("1")
    public void text() throws Exception {
        final String html =
            "<html>\n"
            + "  <head>\n"
            + "    <script>\n"
            + "      function test() {\n"
            + "        execMe('alert(1)');\n"
            + "      }\n"
            + "      function execMe(text) {\n"
            + "        document.head = document.getElementsByTagName('head')[0];\n"
            + "        var script = document.createElement('script');\n"
            + "        script.text = text;\n"
            + "        document.head.appendChild(script);\n"
            + "        document.head.removeChild(script);\n"
            + "      }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "  <body onload='test()'>\n"
            + "  </body>\n"
            + "</html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("onload")
    public void onload_after_deferReadStateComplete() throws Exception {
        final String html =
              "<html>\n"
            + "  <head>\n"
            + "    <title>test</title>\n"
            + "    <script onreadystatechange='if(this.readyState==\"complete\") alert(\"defer\");' defer></script>\n"
            + "  </head>\n"
            + "  <body onload='alert(\"onload\")'>\n"
            + "  </body>\n"
            + "</html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Regression test for bug 47038.
     * http://sourceforge.net/tracker/?func=detail&atid=448266&aid=3403860&group_id=47038
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"1", "2", "3"},
            IE = "1")
    public void scriptType() throws Exception {
        final String html
            = "<html>\n"
            + "<head>\n"
            + "<script type='text/javascript'>alert(1)</script>\n"
            + "<script type=' text/javascript'>alert(2)</script>\n"
            + "<script type=' text/javascript '>alert(3)</script>\n"
            + "<script type=' text / javascript '>alert(4)</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("\n  <ul>{{for people}}\n    <li>Name: {{:name}}</li>\n  {{/for}}</ul>\n")
    public void specialScriptType() throws Exception {
        final String html
            = "<html>\n"
            + "<head>\n"
            + "<script id='template' type='text/x-jsrender'>\n"
            + "  <ul>{{for people}}\n"
            + "    <li>Name: {{:name}}</li>\n"
            + "  {{/for}}</ul>\n"
            + "</script>\n"

            + "<script>\n"
            + "function doTest() {\n"
            + "  script = document.getElementById('template');\n"
            + "  alert(script.innerHTML);\n"
            + "}\n"
            + "</script>\n"

            + "</head>\n"
            + "<body onload='doTest()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test exception throw by IE when calling <code>appendChild</code>.
     * @throws Exception if the test fails
     */
    @Test
    public void appendChild_UnexpectedCall() throws Exception {
        // IE (at least IE6 and IE8) does not support script.appendChild(source)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  var script = document.createElement('script');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.appendChild(source);\n"
              + "  } catch(e) {\n"
              + "    alert(e.message.slice(0,44));\n"
              + "  }\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test exception throw by IE when calling <code>insertBefore</code>.
     * @throws Exception if the test fails
     */
    @Test
    public void insertBeforeUnexpectedCall() throws Exception {
        // IE (at least IE6 and IE8) does not support script.insertBefore(source, null)
        final String html =
                "<html><head><title>foo</title></head><body>\n"
              + "<script>\n"
              + "  var script = document.createElement('script');\n"
              + "  var source = document.createTextNode(\"alert('executed');\");\n"
              + "  try {\n"
              + "    script.insertBefore(source, null);\n"
              + "  } catch(e) {\n"
              + "    alert(e.message.slice(0,44));\n"
              + "  }\n"
              + "</script>\n"
              + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Firefox should not run scripts with "event" and "for" attributes.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("onload for window,")
    public void scriptEventFor() throws Exception {
        final String html = "<html>\n"
            + "<head>\n"
            + "  <script>\n"
            + "    function log(text) {\n"
            + "      var textarea = document.getElementById('myTextarea');\n"
            + "      textarea.value += text + ',';\n"
            + "    }\n"
            + "  </script>\n"
            + "</head><body>\n"
            + "  <textarea id='myTextarea' cols='80' rows='10'></textarea>\n"
            + "  <script event='onload' for='window'>\n"
            + "    log('onload for window');\n"
            + "  </script>\n"
            + "  <div id='div1'>the div 1</div>\n"
            + "  <div id='div2'>the div 2</div>\n"
            + "  <script event='onclick' for='div1'>\n"
            + "    log('onclick for div1');\n"
            + "  </script>\n"
            + "  <script event='onclick' for='document.all.div2'>\n"
            + "    log('onclick for div2');\n"
            + "  </script>\n"
            + "</body></html>";

        final WebDriver webDriver = loadPage2(html);
        webDriver.findElement(By.id("div1")).click();
        webDriver.findElement(By.id("div2")).click();
        assertEquals(getExpectedAlerts()[0], webDriver.findElement(By.id("myTextarea")).getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"function foo() { return a > b}", "function mce() { return a &gt; b}"})
    public void innerHtml() throws Exception {
        final String html
            = "<html><head><title>foo</title>\n"

            + "<script id='script1'>function foo() { return a > b}</script>\n"

            + "<script>\n"
            + "function doTest() {\n"
            + "  script = document.getElementById('script1');\n"
            + "  alert(script.innerHTML);\n"

            + "  script = document.getElementById('mce');\n"
            + "  alert(script.innerHTML);\n"

            + "}\n"
            + "</script>\n"
            + "</head><body onload='doTest()'>\n"
            // this is done by TinyMce
            + "<script>document.write('<mce:script id=\"mce\">function mce() { return a > b}</mce:script>');</script>\n"

            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("\n    <script id=\"testScript\">function foo() { return a > b}</script>\n  ")
    public void innerHTMLGetSet() throws Exception {
        final String html
            = "<html>\n"
            + "<head></head>\n"
            + "<body>\n"

            + "  <div id='tester'>\n"
            + "    <script id='testScript'>function foo() { return a > b}</script>\n"
            + "  </div>\n"

            + "  <script type='text/javascript'>\n"
            + "    var div = document.getElementById('tester');\n"
            + "    try {\n"
            + "      div.innerHTML = div.innerHTML;\n"
            + "    } catch (e) { alert('exception'); }\n"
            + "    alert(div.innerHTML);\n"
            + "  </script>\n"

            + "</body>\n"
            + "</html>\n";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"1", "3", "2"})
    public void async() throws Exception {
        final String html = "<html><body>\n"
            + "<script src='js1.js'></script>\n"
            + "<script src='js2.js' async></script>\n"
            + "<script src='js3.js'></script>\n"
            + "</body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js1.js"), "alert(1);");
        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js2.js"), "alert(2);");
        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js3.js"), "alert(3);");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"2", "1"})
    @NotYetImplemented
    public void async2() throws Exception {
        final String html = "<html><body>\n"
            + "<script>\n"
            + "  var s1 = document.createElement('script');\n"
            + "  s1.src = 'js1.js';\n"
            + "  s1.async = true;\n"
            + "  document.body.appendChild(s1);\n"
            + "</script>\n"
            + "<script src='js2.js'></script>\n"
            + "</body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js1.js"), "alert(1);");
        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js2.js"), "alert(2);");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"2", "1"})
    public void asyncLoadsAsync() throws Exception {
        final String html = "<html><body>\n"
            + "<script async>\n"
            + "  var s1 = document.createElement('script');\n"
            + "  s1.src = 'js1.js';\n"
            + "  s1.async = true;\n"
            + "  document.body.appendChild(s1);\n"
            + "</script>\n"
            + "<script src='js2.js'></script>\n"
            + "</body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js1.js"), "alert(1);");
        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js2.js"), "alert(2);");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"1", "2", "3"})
    public void asyncFromAsyncTask() throws Exception {
        final String html = "<html><body><script>\n"
            + "function addAsyncScript() {\n"
            + "  var script = document.createElement('script');\n"
            + "  script.src = 'js.js';\n"
            + "  script.async = true;\n"
            + "  document.head.appendChild(script);\n"
            + "  alert(2);\n"
            + "}\n"
            + "setTimeout(addAsyncScript, 5);\n"
            + "alert(1);\n"
            + "</script></body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js.js"), "alert(3);");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"undefined", "append", "append done", "from script", "undefined"})
    public void asyncOnLoad() throws Exception {
        final String html = "<html><body>\n"
                + "<script>\n"
                + "  var script = document.createElement('script');\n"
                + "  alert(script.readyState);\n"
                + "  script.src = 'js.js';\n"
                + "  script.async = true;\n"
                + "  script.onload = function () {\n"
                + "    alert(this.readyState);\n"
                + "  };\n"
                + "  alert('append');\n"
                + "  document.body.appendChild(script);\n"
                + "  alert('append done');\n"
                + "</script>\n"
                + "</body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js.js"), "alert('from script');");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"false", "null", "true", "", "true", "", "false", "null"})
    public void asyncProperty() throws Exception {
        final String html = "<html>\n"
            + "<head><title>foo</title>\n"
            + "<script id='script1' src='js1.js'></script>\n"
            + "<script id='script2' src='js2.js' async></script>\n"
            + "<script>\n"
            + "function doTest() {\n"
            + "  var script = document.getElementById('script1');\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script.async = true;\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script = document.getElementById('script2');\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script.async = false;\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "}\n"
            + "</script>\n"
            + "</head><body onload='doTest()'>\n"
            + "</body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js1.js"), "");
        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js2.js"), "");

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"false", "null", "true", "true", "true", "", "true", "true", "false", "null"})
    public void asyncAttribute() throws Exception {
        final String html = "<html>\n"
            + "<head><title>foo</title>\n"
            + "<script id='script1' src='js1.js'></script>\n"
            + "<script id='script2' src='js2.js' async></script>\n"
            + "<script>\n"
            + "function doTest() {\n"
            + "  var script = document.getElementById('script1');\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script.setAttribute('async', true);\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script = document.getElementById('script2');\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script.setAttribute('async', true);\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"

            + "  script.removeAttribute('async');\n"
            + "  alert(script.async);\n"
            + "  alert(script.getAttribute('async'));\n"
            + "}\n"
            + "</script>\n"
            + "</head><body onload='doTest()'>\n"
            + "</body></html>\n";

        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js1.js"), "");
        getMockWebConnection().setResponse(new URL(getDefaultUrl(), "js2.js"), "");

        loadPageWithAlerts2(html);
    }
}
