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

    private JavaScriptBeautifier beautifier_;

    /**
     * {@inheritDoc}
     */
    public boolean init(final Server server, final String prefix) {
        final String className = server.props.getProperty(prefix + "beautifier");
        try {
            beautifier_ = (JavaScriptBeautifier) Class.forName(className).newInstance();
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
        final String beauty = beautifier_.beautify(new String(content));
        return beauty.getBytes();
    }

}
