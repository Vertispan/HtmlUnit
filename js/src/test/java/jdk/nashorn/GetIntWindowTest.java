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
package jdk.nashorn;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;

public class GetIntWindowTest {

    @Test
    public void getInt() throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine.getContext());
        assertEquals("Success", engine.eval("var xxx = window[50];xxx"));
    }

    private void initGlobal(final ScriptContext scriptContext) {
        final Global global = getGlobal(scriptContext);
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            final GetIntWindow window = new GetIntWindow();
            global.put("window", window, true);

        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }
    public static Global getGlobal(final ScriptContext context) {
        return get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object o, final String fieldName) {
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(o);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
