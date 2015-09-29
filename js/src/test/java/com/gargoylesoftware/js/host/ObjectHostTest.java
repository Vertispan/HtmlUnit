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
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

public class ObjectHostTest {

    @Test
    public void object() throws Exception {
        test("[object ObjectHost1]", "ObjectHost1");
        test("IE", "ObjectHost1.parentMethod()");
        test("IE", "ObjectHost2.childMethod()");
    }

    private void test(final String expected, final String script) throws Exception {
        test(expected, script, new Browser(BrowserFamily.IE, 8));
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
            global.put("ObjectHost1", new ObjectHost1.Constructor(), true);
            global.put("ObjectHost2", new ObjectHost2.Constructor(), true);
            setProto(global, "ObjectHost2", "ObjectHost1");
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
        final ScriptObject childObject = (ScriptObject) global.get(childName);
        final ScriptObject parentObject = (ScriptObject) global.get(parentName);
        childObject.setProto(parentObject);
    }

    @Test
    public void inheritance() throws Exception {
        test("IE", "ObjectHost2.parentMethod()");
    }

}
