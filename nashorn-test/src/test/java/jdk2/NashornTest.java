package jdk2;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import jdk2.nashorn.api.scripting.NashornScriptEngineFactory;

public class NashornTest {
    
    static ThreadLocal<String> l = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
//        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        engine.getContext().setAttribute("param1", "paramValue", ScriptContext.ENGINE_SCOPE);
        l.set("hello there");

        engine.put("MyWindow", engine.eval("Java.type(\"" + MyWindow.class.getName() + "\")"));
        String x = "var someString = 'hi';print(typeof someString[Symbol.iterator]);";
        engine.eval(x);
//        engine.eval("print(new MyWindow().test());");
    }

}
