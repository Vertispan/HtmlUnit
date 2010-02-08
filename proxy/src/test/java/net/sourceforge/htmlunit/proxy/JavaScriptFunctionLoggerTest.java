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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
/**
 * Test for {@link JavaScriptFunctionLogger}.
 *
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class JavaScriptFunctionLoggerTest {

    /**
     */
    @Test
    public void test() {
        final String source = "function test() {"
            + "  test2();"
            + "}"
            + "function test2() {"
            + "  alert(window.location.href)"
            + "}";

        final JavaScriptBeautifier beautifier = new JavaScriptFunctionLogger();
        beautifier.setLoggingMethodName("window.top.__HtmlUnitLog");
        final String beautified = beautifier.beautify(source);
        assertTrue(beautified.replaceAll("\\s", "").contains(
            "functiontest(){window.top.__HtmlUnitLog('EnteringMethod:test()');test2();}"));
    }
}
