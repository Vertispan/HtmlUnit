package jdk2;

import java.lang.reflect.Field;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import jdk2.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk2.nashorn.api.scripting.ScriptObjectMirror;
import jdk2.nashorn.internal.objects.Global;
import jdk2.nashorn.internal.objects.PrototypeObject;
import jdk2.nashorn.internal.runtime.Context;
import jdk2.nashorn.internal.runtime.ScriptFunction;
import jdk2.nashorn.internal.runtime.ScriptObject;

public class AbcTest {

    @Test
    public void test() throws Exception {
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine("-HOHO=hehe");
        initGlobal(engine);

        System.out.println(engine.getContext());
        engine.getContext().setAttribute("HOHO", "abcdef", ScriptContext.GLOBAL_SCOPE);
//        System.out.println(engine.getContext(b).getClass().getName());
//        engine.put("Abc", new Abc.Constructor());
//        engine.eval("for (var x in Abc3.prototype) {print(x)}");
//        engine.eval("print(new Int8Array().hasOwnProperty())");
        SimpleBindings b = new SimpleBindings();
//        b.put("HOHO", "something");
//        context.getBindings(ScriptContext.)
        engine.eval("print(Abc3.prototype.hello3())");
        final ScriptObjectMirror mirror = (ScriptObjectMirror) engine.eval("Abc3.prototype");
        final ScriptObject sobj = get(mirror, "sobj");
//        System.out.println(sobj.get("hasOwnProperty"));
//        for (final Object o : sobj.getMap().getProperties()) {
//            System.out.println(o);
//        }
    }

    private void initGlobal(final ScriptEngine engine) {
        SimpleScriptContext c = (SimpleScriptContext) engine.getContext();
        Global global = (Global) ((ScriptObjectMirror) c.getBindings(ScriptContext.ENGINE_SCOPE)).getScriptObject();
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);
            global.put("HOHO", "HEHE", false);
//            global.put("Abc", new Abc.Constructor(), true);
            global.put("Abc3", new Abc3.Constructor(), true);
//            setProto(global, "Abc3", "Abc");
        } finally {
            Context.setGlobal(oldGlobal);
        }
    }

    private void setProto(final Global global, final String childName, final String parentName) {
        ScriptFunction childFunction = (ScriptFunction) global.get(childName);
        PrototypeObject childPrototype = (PrototypeObject) childFunction.getPrototype();
        ScriptFunction parentFunction = (ScriptFunction) global.get(parentName);
        PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
        childPrototype.setProto(parentPrototype);
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object o, final String fieldName) throws Exception {
        final Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(o);
    }
}
