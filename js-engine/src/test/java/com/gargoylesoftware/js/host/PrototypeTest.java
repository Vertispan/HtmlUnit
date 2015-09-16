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
import static org.junit.Assert.assertNotNull;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.api.scripting.ScriptObjectMirror;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;

public class PrototypeTest {

    @Ignore
    @Test
    public void methods() throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        final ScriptContext context = initGlobal(engine);
        assertNotNull(engine.eval("new Int8Array().set", context));
        assertNotNull(engine.eval("new Host1().someMethod", context));
    }

    private ScriptContext initGlobal(final ScriptEngine engine) {
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
        return context;
    }

    @Test
    public void browserInMethods() throws ScriptException {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        initGlobal(engine);
        Browser.setCurent(new Browser(BrowserFamily.CHROME, 45));
        assertEquals("CHROME", engine.eval("Host1.prototype.someMethod()"));

        Browser.setCurent(new Browser(BrowserFamily.IE, 11));
        assertEquals("IE", engine.eval("Host1.prototype.someMethod()"));
    }

}
