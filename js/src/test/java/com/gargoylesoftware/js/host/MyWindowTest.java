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

import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.CHROME;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

public class MyWindowTest {

    @Test
    public void addEventListener() throws Exception {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 45);
        test("[object Object]", "window", chrome);
        test("function Window() { [native code] }", "Window", chrome);
        test("function addEventListener() { [native code] }", "window.addEventListener", chrome);
        final Browser ie11 = new Browser(BrowserFamily.IE, 11);
        test("[object Object]", "window", ie11);
        test("[object Object]", "Window", ie11);
        test("function addEventListener() { [native code] }", "window.addEventListener", ie11);
    }

    private void test(final String expected, final String script, final Browser browser) throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, browser);
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    private void initGlobal(final ScriptEngine engine, final Browser browser) throws Exception {
        Browser.setCurrent(browser);
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);

            final BrowserFamily browserFamily = browser.getFamily();
            if (browserFamily == CHROME) {
                global.put("EventTarget", new MyEventTarget.FunctionConstructor(), true);
                global.put("Window", new MyWindow.FunctionConstructor(), true);
                setProto(global, "Window", "EventTarget");
            }
            else {
                global.put("Window", new MyWindow(), true);
                setProto(global, "Window", new MyEventTarget.ObjectConstructor());
            }

            final MyWindow window = new MyWindow();
            ScriptObject windowProto = Context.getGlobal().getPrototype(window.getClass());
            if (windowProto == null) {
                windowProto = (ScriptObject) global.get("Window");
            }
            window.setProto(windowProto);

            global.put("window", window, true);
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

    private void setProto(final Global global, final String childName, final ScriptObject parentObject) {
        final ScriptObject childObject = (ScriptObject) global.get(childName);
        childObject.setProto(parentObject);
    }
}
