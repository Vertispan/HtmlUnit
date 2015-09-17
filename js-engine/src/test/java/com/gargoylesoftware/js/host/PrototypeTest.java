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
        final Browser browser = new Browser(BrowserFamily.IE, 8);
        test("function set() { [native code] }", "new Int8Array().set", browser);
        test("function someMethod() { [native code] }", "new Host1().someMethod", browser);
    }

    private void test(final String expected, final String script, final Browser browser) throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, browser);
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    @Test
    public void typeofFunction() throws ScriptException {
        final Browser browser = new Browser(BrowserFamily.IE, 8);
        test("function", "typeof new Int8Array().set", browser);
        test("function", "typeof new Host1().someMethod", browser);
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
        final String script = "Host1.prototype.someMethod()";
        test("CHROME", script, new Browser(BrowserFamily.CHROME, 45));
        test("IE", script, new Browser(BrowserFamily.IE, 11));
    }

    @Test
    public void browserSpecificFunction() throws ScriptException {
        final String script = "typeof new Host1().inChromeOnly";
        test("function", script, new Browser(BrowserFamily.CHROME, 45));
        test("undefined", script, new Browser(BrowserFamily.IE, 11));
    }

    @Test
    public void browserSpecificGetter() throws ScriptException {
        test("1", "new Host1().length", new Browser(BrowserFamily.CHROME, 45));
        test("2", "new Host1().length", new Browser(BrowserFamily.IE, 11));
        test("true", "new Host1().length === undefined", new Browser(BrowserFamily.IE, 8));
        test("true", "new Host1().length === undefined", new Browser(BrowserFamily.FF, 38));
    }

    @Test
    public void browserSpecificGetterType() throws ScriptException {
        final String script = "typeof new Host1().length";
        test("number", script, new Browser(BrowserFamily.CHROME, 45));
        test("number", script, new Browser(BrowserFamily.IE, 11));
        test("undefined", script, new Browser(BrowserFamily.IE, 8));
        test("undefined", script, new Browser(BrowserFamily.FF, 38));
    }
}
