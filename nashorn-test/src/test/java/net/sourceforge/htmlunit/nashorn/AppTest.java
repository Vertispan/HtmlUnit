package net.sourceforge.htmlunit.nashorn;

import javax.script.ScriptEngine;

import jdk2.nashorn.api.scripting.NashornScriptEngineFactory;

import org.junit.Test;

/**
 * Simple test.
 */
public class AppTest {

    @Test
    public void test() throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        engine.eval("print('Hello, World!');");
    }

    @Test
    public void int8Array() throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        engine.eval("print(new Int8Array().length);");
    }
}
