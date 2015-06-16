package net.sourceforge.htmlunit.nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.junit.Test;

/**
 * Simple test.
 */
public class AppTest {

    @Test
    public void test() throws Exception {
//        Class.forName("jdk.nashorn.internal.objects.NativeFunction$Constructor");
//        Class.forName("jdk2.nashorn.internal.objects.NativeFunction$Constructor");
        ScriptEngineManager factory = new ScriptEngineManager();
//        ScriptEngine engine = factory.getEngineByName("nashorn");
      ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        System.out.println(engine.getClass().getName());
        try {
            engine.eval("print('Hello, World!');");
        }
        catch (final ScriptException se) {
            se.printStackTrace();
        }
    }
}
