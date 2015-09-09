package jdk2;

import jdk2.nashorn.api.scripting.AbstractJSObject;

public class Abc2 extends AbstractJSObject {

    @Override
    public Object newObject(Object... args) {
        return new Abc2();
    }

    @Override
    public Object getMember(String name) {
        return super.getMember(name);
    }

    @Override
    public String toString() {
        return "Hello";
    }

    public void hi() {
        
    }
}
