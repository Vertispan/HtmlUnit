package net.sourceforge.htmlunit;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ScriptableObject;

/**
 * Test scope of 'eval'.
 *
 * @author Ahmed Ashour
 */
public class EvalScopeTest {

    @Test
	public void eval() {
        eval("host2.eval('id')", 2);
        eval("host2['eval']('id')", 2);
        eval(     "function test(){\n"
                + "  test1(5)\n"
                + "};\n"
                + "function test1(f){\n"
                + "  output=eval('f')\n"
                + "};\n"
                + "var output;\n"
                + "test();\n"
                + "output", 5);
        eval(     "function test(){\n"
                + "test1(5)\n"
                + "};\n"
                + "function test1(f){\n"
                + "output=this['eval']('f')\n"
                + "};\n"
                + "var output;\n"
                + "test();\n"
                + "output", 5);
    }

    private void eval(final String script, final int expected) {
		final ContextAction action = new ContextAction() {
			public Object run(final Context cx) {
				try {
					final MyHostObject prototype = new MyHostObject();
					final Method readMethod = MyHostObject.class.getMethod("jsxGet_id");
					prototype.defineProperty("id", null, readMethod , null, ScriptableObject.EMPTY);

                    final MyHostObject host1 = new MyHostObject(1);
                    host1.setPrototype(prototype);
                    cx.initStandardObjects(host1);

                    final MyHostObject host2 = new MyHostObject(2);
                    host2.setPrototype(prototype);
                    cx.initStandardObjects(host2);

                    ScriptableObject.defineProperty(host1, "host2", host2, ScriptableObject.EMPTY);

                    final Number number = (Number) cx.evaluateString(host1, script, "test_script", 1, null);
                    Assert.assertEquals(expected, number.intValue());
					return null;
				}
				catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		Utils.runWithAllOptimizationLevels(action);
	}

	public static class MyHostObject extends ScriptableObject {
        private int id_;

        private MyHostObject() {
        }

        private MyHostObject(int id) {
            id_ = id;
        }

        @Override
		public String getClassName() {
			return getClass().getSimpleName();
		}

		public int jsxGet_id() {
			return id_;
		}
	}
}
