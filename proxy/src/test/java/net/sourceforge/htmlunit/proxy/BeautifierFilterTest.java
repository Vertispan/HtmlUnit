package net.sourceforge.htmlunit.proxy;

import static org.junit.Assert.assertEquals;

import net.sourceforge.htmlunit.corejs.javascript.Parser;
import net.sourceforge.htmlunit.corejs.javascript.ast.AstRoot;

import org.junit.Test;
/**
 * Test for {@link BeautifierFilter}.
 *
 * @author Ahmed Ashour
 */
public class BeautifierFilterTest {

    /**
     */
    @Test
    public void test() {
        final String source = "function oa(){if(!c.isReady){try{s.documentElement.doScroll(\"left\");}catch(a){"
            + "setTimeout(oa,1);return;}c.ready();}}";

        final String beautified = new BeautifierFilter().beautify(source);
        AstRoot root = new Parser().parse(source, "<cmd>", 0);
        
        //This is for now, to be removed once we are sure the filter prints everything
        assertEquals(root.toSource(), beautified);
        
        assertEquals(source.replaceAll("\\s", ""), beautified.replaceAll("\\s", ""));
        
    }

}
