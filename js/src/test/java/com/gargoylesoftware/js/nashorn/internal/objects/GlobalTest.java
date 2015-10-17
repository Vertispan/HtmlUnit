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
package com.gargoylesoftware.js.nashorn.internal.objects;

import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.Undefined;

/**
 * Test for {@link Global}.
 *
 * @author Ahmed Ashour
 */
public class GlobalTest {

    @Test
    public void arguments() {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
        Context.setGlobal(global);

        global.remove("arguments", true);
    }

    @Test
    public void __FILE__() {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        final SimpleScriptContext context = (SimpleScriptContext) engine.getContext();
        final Global global = get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
        Context.setGlobal(global);

        assertSame(Undefined.getUndefined(), global.get("__FILE__"));
        assertSame(Undefined.getUndefined(), global.get("__LINE__"));
        assertSame(Undefined.getUndefined(), global.get("__DIR__"));
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
