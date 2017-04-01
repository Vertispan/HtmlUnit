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

import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.CHROME;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngine;
import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.api.scripting.ScriptObjectMirror;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeArray;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

public class MyWindowTest {

    @Test
    public void addEventListener() throws Exception {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 55);
        test("[object Window]", "window", chrome);
        test("function Window() { [native code] }", "Window", chrome);
        test("function addEventListener() { [native code] }", "window.addEventListener", chrome);
        final Browser ie = new Browser(BrowserFamily.IE, 11);
        test("[object Window]", "window", ie);
        test("[object Window]", "Window", ie);
        test("function addEventListener() { [native code] }", "window.addEventListener", ie);
    }

    private void test(final String expected, final String script, final Browser browser) throws Exception {
        final NashornScriptEngine engine = createEngine();
        final ScriptContext scriptContext = initGlobal(engine, browser);
        final Object object = engine.eval(script, scriptContext);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    private NashornScriptEngine createEngine() {
        return (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
    }

    private ScriptContext initGlobal(final NashornScriptEngine engine, final Browser browser) throws Exception {
        Browser.setCurrent(browser);
        final Global global = engine.createNashornGlobal();
        final ScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setBindings(new ScriptObjectMirror(global, global), ScriptContext.ENGINE_SCOPE);
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);

            final BrowserFamily browserFamily = browser.getFamily();
            if (browserFamily == CHROME) {
                global.put("EventTarget", new MyEventTarget.FunctionConstructor(), true);
                global.put("Window", new MyWindow.FunctionConstructor(), true);
                setProto(global, "Window", "EventTarget");
                final ScriptFunction parentFunction = (ScriptFunction) global.get("EventTarget");
                final PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
                global.setProto(parentPrototype);
            }
            else {
                global.put("Window", new MyWindow.ObjectConstructor(), true);
                global.setProto(new MyEventTarget.ObjectConstructor());
            }
            
            global.setWindow(new ScriptObject() {});

            global.put("window", global, true);
            return scriptContext;
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    private void setProto(final Global global, final String childName, final String parentName) {
        final Object child = global.get(childName);
        if (child instanceof ScriptFunction) {
            final ScriptFunction childFunction = (ScriptFunction) global.get(childName);
            final PrototypeObject childPrototype = (PrototypeObject) childFunction.getPrototype();
            final ScriptFunction parentFunction = (ScriptFunction) global.get(parentName);
            final PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
            childPrototype.setProto(parentPrototype);
            childFunction.setProto(parentFunction);
        }
        else {
            final ScriptObject childObject = (ScriptObject) global.get(childName);
            final ScriptObject parentObject = (ScriptObject) global.get(parentName);
            childObject.setProto(parentObject);
        }
    }

    @Test
    public void equal() throws Exception {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 55);
        test("true", "this === window", chrome);
        test("true", "window === this", chrome);
        test("true", "this == window", chrome);
        test("true", "window == this", chrome);
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object o, final String fieldName) throws Exception {
        final Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    @Test
    public void calledTwice() throws Exception {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 55);
        final String s = "function info(msg) {\n"
                + "  (function(t){var x = window.__huCatchedAlerts; x = x ? x : []; window.__huCatchedAlerts = x; x.push(String(t))})(msg);"
            + "};\n"
            + "info('a');\n"
            + "info('b')";

        final NashornScriptEngine engine = createEngine();
        final ScriptContext scriptContext = initGlobal(engine, chrome);
        engine.eval(s, scriptContext);

        final String s2 = "window.__huCatchedAlerts";
        final ScriptObjectMirror som = (ScriptObjectMirror) engine.eval(s2, scriptContext);
        final NativeArray result = get(som, "sobj");

        assertEquals(2, result.getLength());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test
    public void testToString() throws Exception {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 55);
        test("function toString() { [native code] }", "toString", chrome);
        final Browser ie = new Browser(BrowserFamily.IE, 11);
        test("function toString() { [native code] }", "toString", ie);
    }

}
