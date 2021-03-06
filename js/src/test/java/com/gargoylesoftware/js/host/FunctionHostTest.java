/*
 * Copyright (c) 2016-2017 Gargoyle Software Inc.
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

import java.lang.reflect.Field;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.SupportedBrowser;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

public class FunctionHostTest {

    @Test
    public void function() throws Exception {
        test("function set() { [native code] }", "new Int8Array().set");
        test("function someMethod() { [native code] }", "new FunctionHost1().someMethod");
    }

    private static void test(final String expected, final String script) throws Exception {
        test(expected, script, SupportedBrowser.IE);
    }

    private static void test(final String expected, final String script, final SupportedBrowser browser) throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, browser);
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    @Test
    public void typeofFunction() throws Exception {
        test("function", "typeof new Int8Array().set");
        test("function", "typeof new FunctionHost1().someMethod");
    }

    private static void initGlobal(final ScriptEngine engine, final SupportedBrowser browser) throws Exception {
        Browser.setCurrent(browser);
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            global.setWindow(new ScriptObject() {});
            global.put("FunctionHost1", new FunctionHost1.FunctionConstructor(), true);
            global.put("FunctionHost2", new FunctionHost2.Constructor(), true);
            setProto(global, "FunctionHost2", "FunctionHost1");
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object o, final String fieldName) throws Exception {
        final Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    private static void setProto(final Global global, final String childName, final String parentName) {
        final ScriptFunction childFunction = (ScriptFunction) global.get(childName);
        final PrototypeObject childPrototype = (PrototypeObject) childFunction.getPrototype();
        final ScriptFunction parentFunction = (ScriptFunction) global.get(parentName);
        final PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
        childPrototype.setProto(parentPrototype);
        childFunction.setProto(parentFunction);
    }

    @Test
    public void browserInMethods() throws Exception {
        final String script = "FunctionHost1.prototype.someMethod()";
        test("CHROME", script, SupportedBrowser.CHROME);
        test("IE", script, SupportedBrowser.IE);
    }

    @Test
    public void browserSpecificFunction() throws Exception {
        final String script = "typeof new FunctionHost1().inChromeOnly";
        test("function", script, SupportedBrowser.CHROME);
        test("undefined", script, SupportedBrowser.IE);
    }

    @Test
    public void browserSpecificGetter() throws Exception {
        test("1", "new FunctionHost1().length", SupportedBrowser.CHROME);
        test("2", "new FunctionHost1().length", SupportedBrowser.IE);
        test("false", "new FunctionHost1().length === undefined", SupportedBrowser.IE);
        test("true", "new FunctionHost1().length === undefined", SupportedBrowser.FF);
    }

    @Test
    public void browserSpecificGetterType() throws Exception {
        final String script = "typeof new FunctionHost1().length";
        test("number", script, SupportedBrowser.CHROME);
        test("number", script, SupportedBrowser.IE);
        test("undefined", script, SupportedBrowser.FF);
    }

    @Test
    public void prototype() throws Exception {
        test("[object Object]", "Object.prototype");
        test("[object FunctionHost1]", "FunctionHost1.prototype");
        test("true", "Object.prototype.prototype === undefined");
        test("true", "FunctionHost1.prototype.prototype === undefined");
        test("true", "new Object().prototype === undefined");
        test("true", "new FunctionHost1().prototype === undefined");
    }

    @Test
    public void __proto__() throws Exception {
        test("function () {}", "Object.__proto__");
        test("function () {}", "Int8Array.__proto__");
        test("function () {}", "FunctionHost1.__proto__");
        test("function FunctionHost1() { [native code] }", "FunctionHost2.__proto__");
        test("[object Object]", "new Object().__proto__");
    }

    @Test
    public void inheritance() throws Exception {
        test("IE", "new FunctionHost2().someMethod()");
    }

    @Test
    public void hierarchy() throws Exception {
        test("function FunctionHost2() { [native code] }", "FunctionHost2");
        test("function FunctionHost1() { [native code] }", "FunctionHost2.__proto__");
        test("function () {}",              "FunctionHost2.__proto__.__proto__");
        test("[object Object]",                            "FunctionHost2.__proto__.__proto__.__proto__");
        test("null",                                       "FunctionHost2.__proto__.__proto__.__proto__.__proto__");
    }

    @Test
    public void scriptFunction() throws Exception {
        test("function abc(def) {}", "var host = new FunctionHost1(); host.fun = function abc(def) {}; host.fun");
    }

    @Test
    public void arbitraryProperty() throws Exception {
        test("new thing", "new FunctionHost1().something");
    }

    @Test
    public void self() throws Exception {
        test(FunctionHost1.class.getName(), "new FunctionHost1().mySelf");
    }
}
