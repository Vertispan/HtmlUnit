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
package com.gargoylesoftware.js;

import static org.junit.Assert.assertEquals;

import javax.script.ScriptEngine;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptRuntime;

public class InitialTest {

    @Test
    public void test() throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        engine.eval("print('Hello, World!');");
    }

    @Test
    public void int8Array() throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        assertEquals(0, (int) engine.eval("new Int8Array().length"));
    }

    @Test
    public void regExp() throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        assertEquals("function RegExp() { [native code] }", ScriptRuntime.safeToString(engine.eval("RegExp")));
    }

}
