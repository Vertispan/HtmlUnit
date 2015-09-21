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
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;

public class FunctionHostTest {

    @Test
    public void function() throws ScriptException {
        test("function set() { [native code] }", "new Int8Array().set");
        test("function someMethod() { [native code] }", "new FunctionHost1().someMethod");
    }

    private void test(final String expected, final String script) throws ScriptException {
        test(expected, script, new Browser(BrowserFamily.IE, 8));
    }

    private void test(final String expected, final String script, final Browser browser) throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, browser);
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    @Test
    public void typeofFunction() throws ScriptException {
        test("function", "typeof new Int8Array().set");
        test("function", "typeof new FunctionHost1().someMethod");
    }

    private void initGlobal(final ScriptEngine engine, final Browser browser) {
        Browser.setCurrent(browser);
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = (Global) ((ScriptObjectMirror) context.getBindings(ScriptContext.ENGINE_SCOPE)).getScriptObject();
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            global.put("FunctionHost1", new FunctionHost1.Constructor(), true);
            global.put("FunctionHost2", new FunctionHost2.Constructor(), true);
            setProto(global, "FunctionHost2", "FunctionHost1");
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    private void setProto(final Global global, final String childName, final String parentName) {
        final ScriptFunction childFunction = (ScriptFunction) global.get(childName);
        final PrototypeObject childPrototype = (PrototypeObject) childFunction.getPrototype();
        final ScriptFunction parentFunction = (ScriptFunction) global.get(parentName);
        final PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
        childPrototype.setProto(parentPrototype);
        childFunction.setProto(parentFunction);
    }

    @Test
    public void browserInMethods() throws ScriptException {
        final String script = "FunctionHost1.prototype.someMethod()";
        test("CHROME", script, new Browser(BrowserFamily.CHROME, 45));
        test("IE", script, new Browser(BrowserFamily.IE, 11));
    }

    @Test
    public void browserSpecificFunction() throws ScriptException {
        final String script = "typeof new FunctionHost1().inChromeOnly";
        test("function", script, new Browser(BrowserFamily.CHROME, 45));
        test("undefined", script, new Browser(BrowserFamily.IE, 11));
    }

    @Test
    public void browserSpecificGetter() throws ScriptException {
        test("1", "new FunctionHost1().length", new Browser(BrowserFamily.CHROME, 45));
        test("2", "new FunctionHost1().length", new Browser(BrowserFamily.IE, 11));
        test("true", "new FunctionHost1().length === undefined", new Browser(BrowserFamily.IE, 8));
        test("true", "new FunctionHost1().length === undefined", new Browser(BrowserFamily.FF, 38));
    }

    @Test
    public void browserSpecificGetterType() throws ScriptException {
        final String script = "typeof new FunctionHost1().length";
        test("number", script, new Browser(BrowserFamily.CHROME, 45));
        test("number", script, new Browser(BrowserFamily.IE, 11));
        test("undefined", script, new Browser(BrowserFamily.IE, 8));
        test("undefined", script, new Browser(BrowserFamily.FF, 38));
    }

    @Test
    public void prototype() throws ScriptException {
        test("[object Object]", "Object.prototype");
        test("[object FunctionHost1]", "FunctionHost1.prototype");
        test("true", "Object.prototype.prototype === undefined");
        test("true", "FunctionHost1.prototype.prototype === undefined");
        test("true", "new Object().prototype === undefined");
        test("true", "new FunctionHost1().prototype === undefined");
    }

    @Test
    public void __proto__() throws ScriptException {
        test("function () { [native code] }", "Object.__proto__");
        test("function () { [native code] }", "Int8Array.__proto__");
        test("function () { [native code] }", "FunctionHost1.__proto__");
        test("function FunctionHost1() { [native code] }", "FunctionHost2.__proto__");
        test("[object Object]", "new Object().__proto__");
    }

    @Test
    public void inheritance() throws ScriptException {
        test("IE", "new FunctionHost2().someMethod()");
    }

    @Test
    public void hierarchy() throws ScriptException {
        test("function FunctionHost2() { [native code] }", "FunctionHost2");
        test("function FunctionHost1() { [native code] }", "FunctionHost2.__proto__");
        test("function () { [native code] }",              "FunctionHost2.__proto__.__proto__");
        test("[object Object]",                            "FunctionHost2.__proto__.__proto__.__proto__");
        test("null",                                       "FunctionHost2.__proto__.__proto__.__proto__.__proto__");
    }
}
