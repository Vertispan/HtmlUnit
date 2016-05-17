package com.gargoylesoftware.js.nashorn.internal.parser;

import static org.junit.Assert.assertEquals;

import javax.script.ScriptEngine;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;

public class ParserTest {

    /**
     * Gracefully handle {@code return} statements in .
     */
    @Test
    public void returnStatement() throws Exception {
        final String script = ""
                + "var x = 1 * 2 * 3;\n"
                + "return x";
        test("6", script);
    }

    private void test(final String expected, final String script) throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }
}
