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
