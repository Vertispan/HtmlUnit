package net.sourceforge.htmlunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * Test that Object properties are returned in the creation order.
 * @author Marc Guillemot
 */
@RunWith(RhinoPatchedRunner.class)
public class PropertiesOrderTest {

	/**
	 * Test for properties order as exposed in issues
	 * https://bugzilla.mozilla.org/show_bug.cgi?id=419090
	 * and
	 * https://bugzilla.mozilla.org/show_bug.cgi?id=383592
	 * @throws Exception if the test fails
	 */
	@Test
	public void testPropertiesOrder() throws Exception {
		final String script = "var o = {a: 1, b: 1, c: 1, d: 1};\n"
			+ "var str = '';\n"
			+ "for (var i in o) str += i + ' ';\n"
			+ "if (str != 'a b c d ')\n"
			+ "  throw 'Got: ' + str";
		
		executeScript(script);
	}

	private void executeScript(final String script) {
		final ContextAction action = new ContextAction()
		{
			public Object run(Context cx) {
				final Scriptable scope = cx.initStandardObjects();
				return cx.evaluateString(scope, script, "test", 0, null);
			}
		};
		ContextFactory.getGlobal().call(action);
	}
}
