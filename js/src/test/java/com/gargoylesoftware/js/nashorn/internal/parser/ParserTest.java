/*
 * Copyright (c) 2016 Gargoyle Software Inc.
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
package com.gargoylesoftware.js.nashorn.internal.parser;

import static org.junit.Assert.assertEquals;

import javax.script.ScriptEngine;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;

public class ParserTest {

    /**
     * Gracefully handle {@code return} statements outside function.
     */
    @Test
    public void returnStatement() throws Exception {
        final String script = ""
                + "var x = 1 * 2 * 3;\n"
                + "return x";
        test("6", script);
    }

    private void test(final String expected, final String script) throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }
}
