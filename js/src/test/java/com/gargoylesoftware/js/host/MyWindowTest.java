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

public class MyWindowTest {

    @Test
    public void addEventListener() throws ScriptException {
        final Browser chrome = new Browser(BrowserFamily.CHROME, 45);
        test("[object Object]", "window", chrome);
        test("function Window() { [native code] }", "Window", chrome);
        test("function addEventListener() { [native code] }", "window.addEventListener", chrome);
//        final Browser ie11 = new Browser(BrowserFamily.IE, 11);
//        test("[object Object]", "window", ie11);
//        test("[object Object]", "Window", ie11);
//        test("function addEventListener() { [native code] }", "window.addEventListener", ie11);
    }

    private void test(final String expected, final String script, final Browser browser) throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, browser);
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    private void initGlobal(final ScriptEngine engine, final Browser browser) {
        Browser.setCurrent(browser);
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = (Global) ((ScriptObjectMirror) context.getBindings(ScriptContext.ENGINE_SCOPE)).getScriptObject();
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            global.put("EventTarget", new MyEventTarget.Constructor(), true);
            global.put("Window", new MyWindow.Constructor(), true);
            setProto(global, "Window", "EventTarget");

            final MyWindow window = new MyWindow();
            window.setProto(Context.getGlobal().getPrototype(window.getClass()));
            global.put("window", window, true);
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

}
