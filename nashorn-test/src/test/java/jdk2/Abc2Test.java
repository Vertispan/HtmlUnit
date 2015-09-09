package jdk2;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Consumer;

import org.junit.Test;

public class Abc2Test {

    @Test
    public void test() throws Exception {
        Lookup lookup = MethodHandles.lookup();
        
        java.lang.reflect.Field f;
        Consumer<?> o = this::hi;
        System.out.println(o.getClass().getName());
//        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
//        engine.put("Abc2", engine.eval("Java.type(\"" + Abc2.class.getName() + "\")"));
//        engine.eval("print(Abc2)");
    }

    public void hi(Object o) {
        
    }
}
