package net.sourceforge.htmlunit.nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jdk2.nashorn.api.scripting.NashornScriptEngineFactory;

import org.junit.Test;

/**
 * Simple test.
 */
public class AppTest {

    @Test
    public void test() throws Exception {
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
