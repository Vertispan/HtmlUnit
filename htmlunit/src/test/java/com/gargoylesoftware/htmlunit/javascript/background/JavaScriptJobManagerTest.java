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
package com.gargoylesoftware.htmlunit.javascript.background;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebTestCase;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInlineFrame;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests for {@link JavaScriptJobManagerImpl}.
 *
 * @version $Revision$
 * @author Brad Clarke
 * @author Ahmed Ashour
 */
public class JavaScriptJobManagerTest extends WebTestCase {

    private long startTime_;

    private void startTimedTest() {
        startTime_ = System.currentTimeMillis();
    }

    private void assertMaxTestRunTime(final long maxRunTimeMilliseconds) {
        final long endTime = System.currentTimeMillis();
        final long runTime = endTime - startTime_;
        assertTrue("\nTest took too long to run and results may not be accurate. Please try again. "
            + "\n  Actual Run Time: "
            + runTime
            + "\n  Max Run Time: "
            + maxRunTimeMilliseconds, runTime < maxRunTimeMilliseconds);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void setClearTimeoutUsesManager() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    var threadID;\n"
            + "    function test() {\n"
            + "      threadID = setTimeout(doAlert, 10000);\n"
            + "    }\n"
            + "    function doAlert() {\n"
            + "      alert('blah');\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "<a onclick='clearTimeout(threadID);' id='clickme'/>\n"
            + "</body>\n"
            + "</html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        startTimedTest();
        final HtmlPage page = loadPage(content, collectedAlerts);
        final JavaScriptJobManager jobManager = page.getEnclosingWindow().getJobManager();
        assertNotNull(jobManager);
        assertEquals(1, jobManager.getJobCount());
        final HtmlAnchor a = page.getHtmlElementById("clickme");
        a.click();
        jobManager.waitForAllJobsToFinish(7000);
        assertEquals(0, jobManager.getJobCount());
        assertEquals(Collections.EMPTY_LIST, collectedAlerts);
        assertMaxTestRunTime(10000);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void setClearIntervalUsesManager() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    var threadID;\n"
            + "    function test() {\n"
            + "      threadID = setInterval(doAlert, 100);\n"
            + "    }\n"
            + "    var iterationNumber=0;\n"
            + "    function doAlert() {\n"
            + "      alert('blah');\n"
            + "      if (++iterationNumber >= 3) {\n"
            + "        clearInterval(threadID);\n"
            + "      }\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body>\n"
            + "</html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        startTimedTest();
        final HtmlPage page = loadPage(content, collectedAlerts);
        final JavaScriptJobManager jobManager = page.getEnclosingWindow().getJobManager();
        assertNotNull(jobManager);
        assertEquals(1, jobManager.getJobCount());
        jobManager.waitForAllJobsToFinish(1000);
        assertEquals(0, jobManager.getJobCount());
        assertEquals(Collections.nCopies(3, "blah"), collectedAlerts);
        assertMaxTestRunTime(1000);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void navigationStopThreadsInChildWindows() throws Exception {
        final String firstContent = "<html><head><title>First</title></head><body>\n"
            + "<iframe id='iframe1' src='"
            + URL_SECOND
            + "'>\n"
            + "<a href='"
            + URL_THIRD.toExternalForm()
            + "' id='clickme'>click me</a>\n"
            + "</body></html>";
        final String secondContent = "<html><head><title>Second</title></head><body>\n"
            + "<script>\n"
            + "setInterval('', 10000);\n"
            + "</script>\n"
            + "</body></html>";
        final String thirdContent = "<html><head><title>Third</title></head><body></body></html>";
        final WebClient client = new WebClient();

        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        webConnection.setResponse(URL_THIRD, thirdContent);

        client.setWebConnection(webConnection);

        final HtmlPage page = client.getPage(URL_FIRST);
        final HtmlInlineFrame iframe = page.getHtmlElementById("iframe1");
        final JavaScriptJobManager mgr = iframe.getEnclosedWindow().getJobManager();
        Assert.assertEquals("inner frame should show child thread", 1, mgr.getJobCount());

        final HtmlAnchor anchor = page.getHtmlElementById("clickme");
        final HtmlPage newPage = anchor.click();

        Assert.assertEquals("new page should load", "Third", newPage.getTitleText());
        Assert.assertEquals("frame should be gone", 0, newPage.getFrames().size());

        mgr.waitForAllJobsToFinish(1000);
        Assert.assertEquals("thread should stop", 0, mgr.getJobCount());
    }

    private String getContent(final String resourceName) throws IOException {
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(resourceName);
            return IOUtils.toString(in);
        }
        finally {
            in.close();
        }
    }

    /**
     * Test for http://sourceforge.net/tracker/index.php?func=detail&aid=1997280&group_id=47038&atid=448266.
     * @throws Exception if the test fails
     */
    @Test
    public void contextFactory_Browser() throws Exception {
        final String firstHtml =
            "<html>\n"
            + "<head>\n"
            + "   <title>1</title>\n"
            + "   <script src='" + URL_THIRD + "' type='text/javascript'></script>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "   setTimeout(finishCreateAccount, 2500);\n"
            + "   function finishCreateAccount() {\n"
            + "       completionUrl = '" + URL_SECOND + "';\n"
            + "       document.location.replace(completionUrl);\n"
            + "   }\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>";
        final String secondHtml =
            "<html>\n"
            + "<head>\n"
            + "   <title>2</title>\n"
            + "   <script src='" + URL_THIRD + "' type='text/javascript'></script>\n"
            + "</head>\n"
            + "<body onload='alert(2)'>\n"
            + "<div id='id2'>Page2</div>\n"
            + "</body>\n"
            + "</html>";

        final String[] expectedAlerts = {"2"};
        final List<String> collectedAlerts = new ArrayList<String>();
        final WebClient webClient =  new WebClient(BrowserVersion.FIREFOX_2);
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final MockWebConnection webConnection = new MockWebConnection();
        webClient.setWebConnection(webConnection);

        webConnection.setResponse(URL_FIRST, firstHtml);
        webConnection.setResponse(URL_SECOND, secondHtml);
        webConnection.setResponse(URL_THIRD, getContent("prototype/1.6.0/dist/prototype.js"), "text/javascript");

        final HtmlPage initialPage = webClient.getPage(URL_FIRST);
        initialPage.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(5000);
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Test for bug 1728883 that makes sure closing a window prevents a
     * recursive setTimeout from continuing forever.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void interruptAllWithRecursiveSetTimeout() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    var threadID;\n"
            + "    function test() {\n"
            + "      alert('ping');\n"
            + "      threadID = setTimeout(test, 5);\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body>\n"
            + "</html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(content, collectedAlerts);
        final JavaScriptJobManager jobManager = page.getEnclosingWindow().getJobManager();
        assertNotNull(jobManager);

        // Not perfect, but 100 chances to start should be enough for a loaded system
        Thread.sleep(500);

        Assert.assertFalse("At least one alert should have fired by now", collectedAlerts.isEmpty());
        ((TopLevelWindow) page.getEnclosingWindow()).close();

        // 100 chances to stop
        jobManager.waitForAllJobsToFinish(500);

        final int finalValue = collectedAlerts.size();

        // 100 chances to fail
        jobManager.waitForAllJobsToFinish(500);

        Assert.assertEquals("No new alerts should have happened", finalValue, collectedAlerts.size());
        page.getWebClient().closeAllWindows();
    }

    /**
     * Unit testing an internal method.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void getNextSchduledJob() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    var threadID1;\n"
            + "    var threadID2;\n"
            + "    var threadID3;\n"
            + "    function test() {\n"
            + "      threadID1 = setTimeout(doAlert, 10000);\n"
            + "      threadID2 = setTimeout(doAlert, 20000);\n"
            + "      threadID3 = setTimeout(doAlert, 30000);\n"
            + "    }\n"
            + "    function doAlert() {\n"
            + "      alert('blah');\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "<a onclick='clearTimeout(threadID1);' id='clickme1'/>\n"
            + "</body>\n"
            + "</html>";

        final HtmlPage page = loadPage(content);
        final JavaScriptJobManagerImpl jobManager = (JavaScriptJobManagerImpl) page
                .getEnclosingWindow()
                .getJobManager();
        assertNotNull(jobManager);
        assertEquals(3, jobManager.getJobCount());
        long nextDelay = jobManager.getNextStartingJob().getDelay(TimeUnit.MILLISECONDS);
        assertTrue(nextDelay < 10001);
        final HtmlAnchor anchor = page.getHtmlElementById("clickme1");
        anchor.click();
        nextDelay = jobManager.getNextStartingJob().getDelay(TimeUnit.MILLISECONDS);
        assertEquals(2, jobManager.getJobCount());
        assertTrue(nextDelay > 10000 && nextDelay < 20001);
    }
}
