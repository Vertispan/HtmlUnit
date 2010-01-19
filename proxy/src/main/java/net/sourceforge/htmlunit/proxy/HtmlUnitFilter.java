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
package net.sourceforge.htmlunit.proxy;

import java.io.IOException;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * HtmlUnit Filter, initial version.
 *
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class HtmlUnitFilter implements Filter {

    /**
     * {@inheritDoc}
     */
    public byte[] filter(final Request arg0, final MimeHeaders arg1, final byte[] arg2) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldFilter(final Request arg0, final MimeHeaders arg1) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean init(final Server arg0, final String arg1) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean respond(final Request arg0) throws IOException {
        return false;
    }

}
