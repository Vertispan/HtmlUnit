/*
 * Copyright (c) 2016 Gargoyle Software Inc.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (http://www.gnu.org/licenses/).
 */
package com.gargoylesoftware.js.nashorn.internal.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngine;
import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.api.scripting.ScriptObjectMirror;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;

public class ScriptFunctionTest {

    @Test
    public void simpleScriptFunction() throws Exception {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 55);
        final NashornScriptEngine engine = createEngine();
        final Global global = initGlobal(engine, chrome);
        final ScriptContext scriptContext = global.getScriptContext();
        final String script = "return 'a' + 'b'";
        Object value = engine.eval(script, scriptContext);
        assertEquals("ab", value);

        final Source source = Source.sourceFor("some name", script);
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            final ScriptFunction eventHandler = global.getContext().compileScript(source, global);
            value = ScriptRuntime.apply(eventHandler, global, "hello");
            assertEquals("ab", value);
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    private NashornScriptEngine createEngine() {
        return (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
    }

    private Global initGlobal(final NashornScriptEngine engine, final Browser browser) throws Exception {
        Browser.setCurrent(browser);
        final Global global = engine.createNashornGlobal();
        final ScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setBindings(new ScriptObjectMirror(global, global), ScriptContext.ENGINE_SCOPE);
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);

            global.setWindow(new ScriptObject() {});
            global.setScriptContext(scriptContext);

            return global;
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    @Test
    public void caller() throws Exception {
        final String script = ""
                + "function test() {\n"
                + "  return test2();\n"
                + "}\n"
                + "function test2() {\n"
                + "  return arguments.callee.caller;\n"
                + "}\n"
                + "test()";
        final NashornScriptEngine engine = createEngine();
        final Object value = engine.eval(script);
        assertNotNull(value);
    }

    @Test
    public void caller2() throws Exception {
        final String script = ""
                + "function test() {\n"
                + "  return test2('a');\n"
                + "}\n"
                + "function test2(a) {\n"
                + "  return a;\n"
                + "}\n"
                + "test()";
        final NashornScriptEngine engine = createEngine();
        final Object value = engine.eval(script);
        assertEquals("a", value);
    }

}
