/*
 * Copyright (c) 2015 Gargoyle Software Inc.
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
package com.gargoylesoftware.js.host;

import static org.junit.Assert.assertEquals;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.api.scripting.ScriptObjectMirror;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;

public class PrototypeTest {

    @Test
    public void function() throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, new Browser(BrowserFamily.IE, 8));
        assertEquals("function set() { [native code] }", engine.eval("new Int8Array().set").toString());
        assertEquals("function someMethod() { [native code] }", engine.eval("new Host1().someMethod").toString());
    }

    @Test
    public void typeofFunction() throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, new Browser(BrowserFamily.IE, 8));
        assertEquals("function", engine.eval("typeof new Int8Array().set").toString());
        assertEquals("function", engine.eval("typeof new Host1().someMethod").toString());
    }

    private void initGlobal(final ScriptEngine engine, final Browser browser) {
        Browser.setCurrent(browser);
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = (Global) ((ScriptObjectMirror) context.getBindings(ScriptContext.ENGINE_SCOPE)).getScriptObject();
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            global.put("Host1", new Host1.Constructor(), true);
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    @Test
    public void browserInMethods() throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, new Browser(BrowserFamily.CHROME, 45));
        assertEquals("CHROME", engine.eval("Host1.prototype.someMethod()"));

        Browser.setCurrent(new Browser(BrowserFamily.IE, 11));
        assertEquals("IE", engine.eval("Host1.prototype.someMethod()"));
    }

    @Test
    public void browserSpecificFunction() throws ScriptException {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, new Browser(BrowserFamily.CHROME, 45));
        assertEquals("function", engine.eval("typeof new Host1().inChromeOnly"));

        engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, new Browser(BrowserFamily.IE, 11));
        assertEquals("undefined", engine.eval("typeof new Host1().inChromeOnly"));
    }

}
