package net.sourceforge.htmlunit.nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jdk2.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk2.nashorn.internal.objects.annotations.Function;

import org.junit.Test;

/**
 * Simple test.
 */
public class AppTest {

    @Test
    public void test() throws Exception {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        System.out.println(engine.getClass().getName());
//        engine.put("alert", AppTest.class.getDeclaredMethod("alert", String.class));
        try {
            engine.eval("print('Hello, World!');");
        }
        catch (final ScriptException se) {
            se.printStackTrace();
        }
    }

    @Function
    public static void alert(String x) {
        System.out.println("alert " + x);
    }
}
