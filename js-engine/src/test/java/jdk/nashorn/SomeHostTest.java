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
package jdk.nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class SomeHostTest {

    @Test
    public void methods() throws ScriptException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.put("SomeHost", engine.eval("Java.type('" + SomeHost.class.getName() + "')"));

        System.out.println(engine.eval("new Int8Array()"));
        System.out.println(engine.eval("new SomeHost()"));
        
        System.out.println(engine.eval("new Int8Array().set"));

        // prints "in aMethod()", should be "function aMethod() { [native code] }"
        System.out.println(engine.eval("new SomeHost().aMethod"));
    }

}
