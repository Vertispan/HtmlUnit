package net.sourceforge.htmlunit;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * String conversion of Arguments array used to be '[object Object]',
 * with EcmaScript 5 it is now '[object Arguments]' what is not good for HtmlUnit.
 *
 * @author Marc Guillemot
 * @author Ahmed Ashour
 */
public class ArgumentsTest {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void argumentstoString() throws Exception {
        argumentstoString(true, "[object Object]");
        argumentstoString(false, "[object Arguments]");
    }

    private void argumentstoString(final boolean hasFeature, final String expected) throws Exception {

        final ContextFactory cf = new ContextFactory() {
            @Override
            protected boolean hasFeature(Context cx, int featureIndex) {
                if (Context.FEATURE_HTMLUNIT_ARGUMENTS_IS_OBJECT == featureIndex) {
                    return hasFeature;
                }
                return super.hasFeature(cx, featureIndex);
            }
        };
        final String script = "function f() {\n"
                + "  output = arguments.toString();"
                + "}\n"
                + "var output = '';\n"
                + "f();\n"
                + "output";
        final ContextAction action = new ContextAction() {
            public Object run(final Context cx) {
                final Scriptable scope = cx.initStandardObjects();
                final Object result = cx.evaluateString(scope, script, "test.js", 1, null);
                Assert.assertEquals(expected, result);
                return null;
            }
        };

        Utils.runWithAllOptimizationLevels(cf, action);
    }
}
