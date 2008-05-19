/*
 * Copyright (c) 2002-2008 Gargoyle Software Inc.
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A fake WebConnection designed to mock out the actual HTTP connections.
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Noboru Sinohara
 * @author Marc Guillemot
 * @author Brad Clarke
 * @author Ahmed Ashour
 */
public class MockWebConnection extends WebConnectionImpl {
    private final Map<String, WebResponseData> responseMap_ = new HashMap<String, WebResponseData>(10);
    private WebResponseData defaultResponse_;

    private WebRequestSettings lastRequest_;
    private HttpState httpState_ = new HttpState();

    /**
     * Creates an instance.
     *
     * @param webClient the web client
     */
    public MockWebConnection(final WebClient webClient) {
        super(webClient);
    }

    /**
     * Returns the log that is being used for all scripting objects.
     * @return the log
     */
    protected final Log getLog() {
        return LogFactory.getLog(getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(final WebRequestSettings webRequestSettings) throws IOException {
        final URL url = webRequestSettings.getUrl();

        getLog().debug("Getting response for " + url.toExternalForm());

        lastRequest_ = webRequestSettings;

        WebResponseData response = responseMap_.get(url.toExternalForm());
        if (response == null) {
            response = defaultResponse_;
            if (response == null) {
                throw new IllegalStateException("No response specified that can handle URL ["
                    + url.toExternalForm()
                    + "]");
            }
        }

        return new WebResponseImpl(response, webRequestSettings.getCharset(), webRequestSettings, 0);
    }

    /**
     * Returns the method that was used in the last call to submitRequest().
     *
     * @return the method that was used in the last call to submitRequest()
     */
    public HttpMethod getLastMethod() {
        return lastRequest_.getHttpMethod();
    }

    /**
     * Returns the parameters that were used in the last call to submitRequest().
     *
     * @return the parameters that were used in the last call to submitRequest()
     */
    public List<NameValuePair> getLastParameters() {
        return lastRequest_.getRequestParameters();
    }

    /**
     * Sets the response that will be returned when the specified URL is requested.
     * @param url the URL that will return the given response
     * @param content the content to return
     * @param statusCode the status code to return
     * @param statusMessage the status message to return
     * @param contentType the content type to return
     * @param responseHeaders the response headers to return
     */
    public void setResponse(final URL url, final String content, final int statusCode,
            final String statusMessage, final String contentType,
            final List< ? extends NameValuePair> responseHeaders) {

        setResponse(
                url,
                TextUtil.stringToByteArray(content),
                statusCode,
                statusMessage,
                contentType,
                responseHeaders);
    }

    /**
     * Sets the response that will be returned when the specified URL is requested.
     * @param url the URL that will return the given response
     * @param content the content to return
     * @param statusCode the status code to return
     * @param statusMessage the status message to return
     * @param contentType the content type to return
     * @param responseHeaders the response headers to return
     */
    public void setResponse(final URL url, final byte[] content, final int statusCode,
            final String statusMessage, final String contentType,
            final List< ? extends NameValuePair> responseHeaders) {

        final List<NameValuePair> compiledHeaders = new ArrayList<NameValuePair>(responseHeaders);
        compiledHeaders.add(new NameValuePair("Content-Type", contentType));
        final WebResponseData responseEntry = new WebResponseData(content, statusCode, statusMessage, compiledHeaders);
        responseMap_.put(url.toExternalForm(), responseEntry);
    }

    /**
     * Convenient method that is the same as calling
     * {@link #setResponse(URL,String,int,String,String,List)} with a status
     * of "200 OK", a content type of "text/html" and no additional headers.
     *
     * @param url the URL that will return the given response
     * @param content the content to return
     */
    public void setResponse(final URL url, final String content) {
        final List< ? extends NameValuePair> emptyList = Collections.emptyList();
        setResponse(url, content, 200, "OK", "text/html", emptyList);
    }

    /**
     * Convenient method that is the same as calling
     * {@link #setResponse(URL,String,int,String,String,List)} with a status
     * of "200 OK" and no additional headers.
     *
     * @param url the URL that will return the given response
     * @param content the content to return
     * @param contentType the content type to return
     */
    public void setResponse(final URL url, final String content, final String contentType) {
        final List< ? extends NameValuePair> emptyList = Collections.emptyList();
        setResponse(url, content, 200, "OK", contentType, emptyList);
    }

    /**
     * Specify a generic HTML page that will be returned when the given URL is specified.
     * The page will contain only minimal HTML to satisfy the HTML parser but will contain
     * the specified title so that tests can check for titleText.
     *
     * @param url the URL that will return the given response
     * @param title the title of the page
     */
    public void setResponseAsGenericHtml(final URL url, final String title) {
        final String content = "<html><head><title>" + title + "</title></head><body></body></html>";
        setResponse(url, content);
    }

    /**
     * Sets the response that will be returned when a URL is requested that does
     * not have a specific content set for it.
     *
     * @param content the content to return
     * @param statusCode the status code to return
     * @param statusMessage the status message to return
     * @param contentType the content type to return
     */
    public void setDefaultResponse(final String content, final int statusCode,
            final String statusMessage, final String contentType) {

        setDefaultResponse(TextUtil.stringToByteArray(content), statusCode, statusMessage, contentType);
    }

    /**
     * Sets the response that will be returned when a URL is requested that does
     * not have a specific content set for it.
     *
     * @param content the content to return
     * @param statusCode the status code to return
     * @param statusMessage the status message to return
     * @param contentType the content type to return
     */
    public void setDefaultResponse(final byte[] content, final int statusCode,
            final String statusMessage, final String contentType) {

        final List<NameValuePair> compiledHeaders = new ArrayList<NameValuePair>();
        compiledHeaders.add(new NameValuePair("Content-Type", contentType));
        final WebResponseData responseEntry = new WebResponseData(content, statusCode, statusMessage, compiledHeaders);
        defaultResponse_ = responseEntry;
    }

    /**
     * Sets the response that will be returned when a URL is requested that does
     * not have a specific content set for it.
     *
     * @param content the content to return
     */
    public void setDefaultResponse(final String content) {
        setDefaultResponse(content, 200, "OK", "text/html");
    }

    /**
     * Returns the {@link HttpState}.
     * @return the state
     */
    @Override
    public HttpState getState() {
        return httpState_;
    }

    /**
     * Returns the additional headers that were used in the in the last call
     * to {@link #getResponse(WebRequestSettings)}.
     * @return the additional headers that were used in the in the last call
     *         to {@link #getResponse(WebRequestSettings)}
     */
    public Map<String, String> getLastAdditionalHeaders() {
        return lastRequest_.getAdditionalHeaders();
    }

    /**
     * Returns the {@link WebRequestSettings} that was used in the in the last call
     * to {@link #getResponse(WebRequestSettings)}.
     * @return the {@link WebRequestSettings} that was used in the in the last call
     *         to {@link #getResponse(WebRequestSettings)}
     */
    public WebRequestSettings getLastWebRequestSettings() {
        return lastRequest_;
    }
}
