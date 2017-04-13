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
package com.gargoylesoftware.js.nashorn;

import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.CHROME;
import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.FF;
import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.IE;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;

public class ScriptUtilsTest {

    private static void test(final String expected, final String script, final Browser browser) throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine, browser);
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }

    private static void initGlobal(final ScriptEngine engine, final Browser browser) throws Exception {
        Browser.setCurrent(browser);
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
        final TestWindow window = TestWindow.constructor(true, global);

        Context.setGlobal(global);
        global.setWindow(window);
        global.put("Window", new TestWindow.FunctionConstructor(), true);

        final String[] windowProperties = {"top"};
        final PropertyMap propertyMap = window.getMap();
        final List<Property> list = new ArrayList<>();
        for (final String key : windowProperties) {
            final Property property = propertyMap.findProperty(key);
            if (property != null) {
                list.add(property);
            }
        }
        global.setMap(global.getMap().addAll(PropertyMap.newMap(list)));
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object o, final String fieldName) throws Exception {
        final Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(o);
    }

    @Test
    public void setProperty() throws Exception {
        final String script = ""
                + "var output = '';\n"
                + "output += typeof top;\n"
                + "top = 'hello';\n"
                + "output += ', ' + typeof top;\n"
                + "output";
        test("object, object", script, new Browser(CHROME, 50));
        test("object, object", script, new Browser(FF, 45));
        test("object, string", script, new Browser(IE, 11));
    }

}
