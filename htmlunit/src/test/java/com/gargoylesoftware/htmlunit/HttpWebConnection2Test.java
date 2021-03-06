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
package com.gargoylesoftware.htmlunit;

import static com.gargoylesoftware.htmlunit.BrowserRunner.TestedBrowser.IE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

/**
 * Tests methods in {@link HttpWebConnection}.
 *
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class HttpWebConnection2Test extends WebDriverTestCase {

    /**
     * Tests a simple POST request.
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented(IE)
    public void formGet() throws Exception {
        final String html = "<html><body><form action='foo' method='get' accept-charset='iso-8859-1'>\n"
            + "<input name='text1' value='me &amp;amp; you'>\n"
            + "<textarea name='text2'>Hello\nworld!</textarea>\n"
            + "<input type='submit' id='submit'>\n"
            + "</form></body></html>";

        getMockWebConnection().setDefaultResponse("");
        final WebDriver driver = loadPage2(html, URL_FIRST);
        driver.findElement(By.id("submit")).click();

        // better would be to look at the HTTP traffic directly
        // rather than to examine a request that has been rebuilt but...
        final WebRequest lastRequest = getMockWebConnection().getLastWebRequest();

        assertSame(ISO_8859_1, lastRequest.getCharset());
        assertEquals(null, lastRequest.getProxyHost());
        assertEquals(null, lastRequest.getRequestBody());
        // we will check the parameters later on
        assertEquals(URL_FIRST + "foo", UrlUtils.getUrlWithNewQuery(lastRequest.getUrl(), null));

        String expectedHeaders = "";
        if (getBrowserVersion().isChrome()) {
            expectedHeaders = "Connection: keep-alive\n"
                                + "Host: localhost:" + PORT + "\n"
                                + "Referer: http://localhost:" + PORT + "/\n"
                                + "Upgrade-Insecure-Requests: 1\n"
                                + "User-Agent: " + getBrowserVersion().getUserAgent() + "\n";
        }
        if (getBrowserVersion().isFirefox()) {
            expectedHeaders = "Connection: keep-alive\n"
                    + "Host: localhost:" + PORT + "\n"
                    + "Referer: http://localhost:" + PORT + "/\n"
                    + "Upgrade-Insecure-Requests: 1\n"
                    + "User-Agent: " + getBrowserVersion().getUserAgent() + "\n";
        }
        if (getBrowserVersion().isIE()) {
            expectedHeaders = "Connection: keep-alive\n"
                                + "DNT: 1\n"
                                + "Host: localhost:" + PORT + "\n"
                                + "Referer: http://localhost:" + PORT + "/\n"
                                + "User-Agent: " + getBrowserVersion().getUserAgent() + "\n";
        }
        assertEquals(expectedHeaders, headersToString(lastRequest));

        assertEquals(FormEncodingType.URL_ENCODED, lastRequest.getEncodingType());
        assertEquals(HttpMethod.GET, lastRequest.getHttpMethod());
        assertEquals(0, lastRequest.getProxyPort());
        final List<NameValuePair> parameters = lastRequest.getRequestParameters();
        assertEquals(2, parameters.size());
        for (final NameValuePair pair : parameters) {
            if ("text1".equals(pair.getName())) {
                assertEquals("me &amp; you", pair.getValue());
            }
            else {
                assertEquals("Hello\r\nworld!", pair.getValue());
            }
        }
    }

    /**
     * Tests a simple POST request.
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented(IE)
    public void formPost() throws Exception {
        final String html = "<html><body><form action='foo' method='post' accept-charset='iso-8859-1'>\n"
            + "<input name='text1' value='me &amp;amp; you'>\n"
            + "<textarea name='text2'>Hello\nworld!</textarea>\n"
            + "<input type='submit' id='submit'>\n"
            + "</form></body></html>";

        getMockWebConnection().setDefaultResponse("");
        final WebDriver driver = loadPage2(html, URL_FIRST);
        driver.findElement(By.id("submit")).click();

        // better would be to look at the HTTP traffic directly
        // rather than to examine a request that has been rebuilt but...
        final WebRequest lastRequest = getMockWebConnection().getLastWebRequest();

        assertSame(ISO_8859_1, lastRequest.getCharset());
        assertEquals(null, lastRequest.getProxyHost());
        assertEquals(null, lastRequest.getRequestBody());
        assertEquals(URL_FIRST + "foo", lastRequest.getUrl());

        String expectedHeaders = "";
        if (getBrowserVersion().isChrome()) {
            expectedHeaders = "Cache-Control: max-age=0\n"
                                + "Connection: keep-alive\n"
                                + "Content-Length: 48\n"
                                + "Content-Type: application/x-www-form-urlencoded\n"
                                + "Host: localhost:" + PORT + "\n"
                                + "Origin: http://localhost:" + PORT + "\n"
                                + "Referer: http://localhost:" + PORT + "/\n"
                                + "Upgrade-Insecure-Requests: 1\n"
                                + "User-Agent: " + getBrowserVersion().getUserAgent() + "\n";
        }
        if (getBrowserVersion().isFirefox()) {
            expectedHeaders = "Connection: keep-alive\n"
                    + "Content-Length: 48\n"
                    + "Content-Type: application/x-www-form-urlencoded\n"
                    + "Host: localhost:" + PORT + "\n"
                    + "Referer: http://localhost:" + PORT + "/\n"
                    + "Upgrade-Insecure-Requests: 1\n"
                    + "User-Agent: " + getBrowserVersion().getUserAgent() + "\n";
        }
        if (getBrowserVersion().isIE()) {
            expectedHeaders = "Cache-Control: no-cache\n"
                                + "Connection: keep-alive\n"
                                + "Content-Length: 48\n"
                                + "Content-Type: application/x-www-form-urlencoded\n"
                                + "DNT: 1\n"
                                + "Host: localhost:" + PORT + "\n"
                                + "Referer: http://localhost:" + PORT + "/\n"
                                + "User-Agent: " + getBrowserVersion().getUserAgent() + "\n";
        }
        assertEquals(expectedHeaders, headersToString(lastRequest));

        assertEquals(FormEncodingType.URL_ENCODED, lastRequest.getEncodingType());
        assertEquals(HttpMethod.POST, lastRequest.getHttpMethod());
        assertEquals(0, lastRequest.getProxyPort());
        final List<NameValuePair> parameters = lastRequest.getRequestParameters();
        assertEquals(2, parameters.size());
        for (final NameValuePair pair : parameters) {
            if ("text1".equals(pair.getName())) {
                assertEquals("me &amp; you", pair.getValue());
            }
            else {
                assertEquals("Hello\r\nworld!", pair.getValue());
            }
        }
    }

    private static String headersToString(final WebRequest request) {
        // why doesn't HtmlUnit send these headers whereas Firefox does?
        final List<String> ignoredHeaders = Arrays.asList(
                HttpHeader.ACCEPT_LC, "accept-charset", HttpHeader.ACCEPT_ENCODING_LC,
                HttpHeader.ACCEPT_LANGUAGE_LC, "keep-alive");
        final List<String> caseInsensitiveHeaders = Arrays.asList(HttpHeader.CONNECTION_LC);

        // ensure ordering for comparison
        final Map<String, String> headers = new TreeMap<>(request.getAdditionalHeaders());

        final StringBuilder sb = new StringBuilder();
        for (final Entry<String, String> headerEntry : headers.entrySet()) {
            final String headerName = headerEntry.getKey();
            final String headerNameLower = headerName.toLowerCase(Locale.ROOT);
            if (ignoredHeaders.contains(headerNameLower)) {
                continue;
            }
            sb.append(headerName);
            sb.append(": ");
            if (caseInsensitiveHeaders.contains(headerNameLower)) {
                sb.append(headerEntry.getValue().toLowerCase(Locale.ROOT));
            }
            else if (HttpHeader.USER_AGENT_LC.equals(headerNameLower)) {
                // ignore the 64bit difference
                sb.append(headerEntry.getValue().replace("WOW64; ", ""));
            }
            else {
                sb.append(headerEntry.getValue());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Test for broken gzip content.
     * @throws Exception if the test fails
     */
    @Test
    public void brokenGzip() throws Exception {
        final byte[] content = new byte[] {-1};
        final List<NameValuePair> headers = new ArrayList<>();
        headers.add(new NameValuePair("Content-Encoding", "gzip"));
        headers.add(new NameValuePair(HttpHeader.CONTENT_LENGTH, String.valueOf(content.length)));

        final MockWebConnection conn = getMockWebConnection();
        conn.setResponse(URL_FIRST, content, 404, "OK", "text/html", headers);

        // only check that no exception is thrown
        final WebDriver driver = loadPageWithAlerts2(URL_FIRST);
        assertTrue(driver.getPageSource().length() > 100);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void redirectBrokenGzip() throws Exception {
        final String html = "<html></html>";

        final List<NameValuePair> headers = Arrays.asList(new NameValuePair("Location", URL_SECOND.toString()),
                new NameValuePair("Content-Encoding", "gzip"));
        final MockWebConnection conn = getMockWebConnection();
        conn.setResponse(URL_FIRST, "12", 302, "Some error", "text/html", headers);
        conn.setResponse(URL_SECOND, html);

        final WebDriver driver = loadPageWithAlerts2(URL_FIRST);
        assertEquals(URL_SECOND.toString(), driver.getCurrentUrl());
    }

}
