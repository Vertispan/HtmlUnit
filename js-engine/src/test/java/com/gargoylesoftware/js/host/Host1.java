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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("Host1")
public class Host1 extends ScriptObject {

    private static PropertyMap $nasgenmap$;

    public Host1() {
        super($nasgenmap$);
    }

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static Host1 constructor(final boolean newObj, final Object self) {
        return new Host1();
    }

    @Function
    public static String someMethod(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    static {
        final List<Property> list = Collections.emptyList();
        $nasgenmap$ = PropertyMap.newMap(list);
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(Host1.class, name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptFunction {
        public Constructor() {
            super("Host1", staticHandle("constructor", Host1.class, boolean.class, Object.class), null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction someMethod;
        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$someMethod() {
            return this.someMethod;
        }

        public void S$someMethod(final ScriptFunction function) {
            this.someMethod = function;
        }

        static {
            final List<Property> list = new ArrayList<>(1);
            list.add(AccessorProperty.create("someMethod", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$someMethod", ScriptFunction.class),
                    virtualHandle("S$someMethod", void.class, ScriptFunction.class)));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
            super($nasgenmap$);
            someMethod = ScriptFunction.createBuiltin("someMethod",
                    staticHandle("someMethod", String.class, Object.class));
        }

        public String getClassName() {
            return "Host1";
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Prototype.class, name, MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
