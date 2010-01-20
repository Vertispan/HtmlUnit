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
            + "setTimeout(oa,1);return;}c.ready();}}"
            + "function La(a,b){b.src?c.ajax({url:b.src,async:false,dataType:\"script\"}):"
            + "c.globalEval(b.text||b.textContent||b.innerHTML||\"\");b.parentNode&&b.parentNode.removeChild(b);}"
            + "function $(a,b,d,f,e,i){var j=a.length;if(typeof b===\"object\"){for(var o in b)$(a,o,b[o],f,e,d);"
            + "return a;}if(d!==w){f=!i&&f&&c.isFunction(d);for(o=0;o<j;o++)e(a[o],b,f?d.call(a[o],o,e(a[o],b)):d,i);"
            + "return a;}return j?e(a[0],b):null;}";

        final String beautified = new BeautifierFilter().beautify(source);

        assertEquals(source.replaceAll("\\s", ""), beautified.replaceAll("\\s", ""));
    }

}
