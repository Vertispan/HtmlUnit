/*
 * Copyright (c) 2010 HtmlUnit team.
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
package  net.sourceforge.htmlunit.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import org.paw.server.PawMain;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * Beautifier filter.
 *
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class JavaScriptBeautifierFilter implements Filter {

    private static String LOCALHOST_ADDRESS_;
    private static final int SERVER_PORT_ = PawMain.getServer().getPort();
    private JavaScriptBeautifier beautifier_;

    static {
        try {
            LOCALHOST_ADDRESS_ = InetAddress.getLocalHost().getHostAddress();
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean init(final Server server, final String prefix) {
        final String className = server.props.getProperty(prefix + "beautifier");
        try {
            beautifier_ = (JavaScriptBeautifier) Class.forName(className).newInstance();
            beautifier_.setLoggingMethodName("window.top.__HtmlUnitLog");
            return true;
        }
        catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean respond(final Request request) throws IOException {
        if (request.url.endsWith("/__HtmlUnitLogger") && request.postData != null) {
            final String log = new String(request.postData);
            WebApplUtils.addLog(log);
            request.sendResponse("");
            return true;
        }
        String urlString = request.url;
        if (urlString.startsWith("/")) {
            urlString = "http://localhost:" + SERVER_PORT_ + urlString;
        }
        final URL url = new URL(urlString);
        if (url.getPort() == SERVER_PORT_
                && (url.getHost().equals("localhost")
                || InetAddress.getByName(url.getHost()).getHostAddress().equals(LOCALHOST_ADDRESS_))) {
            try {
                WebApplUtils.respond(request);
                return true;
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldFilter(final Request request, final MimeHeaders headers) {
        final String type = headers.get("Content-Type");
        return type != null
            && (type.equals("text/javascript") || type.equals("application/x-javascript"));
    }

    /**
     * {@inheritDoc}
     */
    public byte[] filter(final Request request, final MimeHeaders headers, final byte[] content) {
        String beauty = beautifier_.beautify(new String(content));
        beauty = "if (!window.top.__HtmlUnitLog) {\n"
            + "  window.top.__HtmlUnitLogger = window.XMLHttpRequest "
            + "? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');\n"
            + "  window.top.__HtmlUnitLog = function(data) {\n"
            + "    var req = window.top.__HtmlUnitLogger;\n"
            + "    req.open('POST', '/__HtmlUnitLogger', false);\n"
            + "    req.send(data);\n"
            + "  }\n"
            + "}\n"
            + beauty;
        return beauty.getBytes();
    }

}
