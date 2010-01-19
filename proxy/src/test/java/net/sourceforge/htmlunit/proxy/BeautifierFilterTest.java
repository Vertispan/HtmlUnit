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

import static org.junit.Assert.assertEquals;

import net.sourceforge.htmlunit.corejs.javascript.Parser;
import net.sourceforge.htmlunit.corejs.javascript.ast.AstRoot;

import org.junit.Test;
/**
 * Test for {@link BeautifierFilter}.
 *
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class BeautifierFilterTest {

    /**
     */
    @Test
    public void test() {
        final String source = "function oa(){if(!c.isReady){try{s.documentElement.doScroll(\"left\");}catch(a){"
            + "setTimeout(oa,1);return;}c.ready();}}";

        final String beautified = new BeautifierFilter().beautify(source);
        final AstRoot root = new Parser().parse(source, "<cmd>", 0);

        //This is for now, to be removed once we are sure the filter prints everything
        assertEquals(root.toSource(), beautified);

        assertEquals(source.replaceAll("\\s", ""), beautified.replaceAll("\\s", ""));
    }

}
