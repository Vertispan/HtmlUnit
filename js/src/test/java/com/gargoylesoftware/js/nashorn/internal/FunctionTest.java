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
package com.gargoylesoftware.js.nashorn.internal;

import static org.junit.Assert.assertEquals;

import javax.script.ScriptEngine;

import org.junit.Test;

import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;

public class FunctionTest {

    /**
     * Function {@code .arguments} should always be set.
     */
    @Test
    public void functionPrototypeArguments() throws Exception {
        final String script = ""
                + "var output = '';\n"
                + "Function.prototype.doAlerts = function(a,b,c) {\n"
                + "  var value = this.arguments;\n"
                + "  output += (value ? value.length : value);\n"
                + "}\n"
                + "\n"
                + "var o = function() {};\n"
                + "o.f = function(x, y, z) {\n"
                + "  this.f.doAlerts();\n"
                + " }\n"
                + "o.f('a', 'b');\n"
                + "output";
        test("2", script);
    }

    /**
     * {@code arguments.callee.arguments} should always be set.
     */
    @Test
    public void functionPrototypeArguments2() throws Exception {
        final String script = ""
                + "var output = '';\n"
                + "Function.prototype.doAlerts = function(a,b,c) {\n"
                + "}\n"
                + "\n"
                + "var o = function() {};\n"
                + "o.f = function(x, y, z) {\n"
                + "  var value = arguments.callee.arguments;\n"
                + "  output += (value ? value.length : value);\n"
                + "  this.f.doAlerts();\n"
                + " }\n"
                + "o.f('a', 'b');\n"
                + "output";
        test("2", script);
    }

    private void test(final String expected, final String script) throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        final Object object = engine.eval(script);
        assertEquals(expected, object == null ? "null" : object.toString());
    }
}
