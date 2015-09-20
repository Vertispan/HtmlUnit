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

import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.CHROME;
import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily.IE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.WebBrowser;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("Host2")
public class Host2 extends ScriptObject {

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static Host2 constructor(final boolean newObj, final Object self) {
        final Host2 host = new Host2();
        host.setProto(Context.getGlobal().getPrototype(host.getClass()));
        return host;
    }

    @Function
    public static String someMethod2(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Function(browsers = @WebBrowser(CHROME))
    public static String inChromeOnly2(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Getter(browsers = {@WebBrowser(value = IE, minVersion = 11), @WebBrowser(CHROME) })
    public static int length2(final Object self) {
        return Browser.getCurrent().getFamily() == CHROME ? 1 : 2;
    }

    {
        final List<Property> list = new ArrayList<>(1);
        final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
        final int browserVersion = Browser.getCurrent().getVersion();
        if ((browserFamily == IE && browserVersion >= 11) || browserFamily == CHROME) {
            list.add(AccessorProperty.create("length2", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    staticHandle("length2", int.class, Object.class),
                    null));
        }
        setMap(PropertyMap.newMap(list));
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(Host2.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptFunction {
        public Constructor() {
            super("Host2", 
                    staticHandle("constructor", Host2.class, boolean.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction someMethod2;
        private ScriptFunction inChromeOnly2;

        public ScriptFunction G$someMethod2() {
            return this.someMethod2;
        }

        public void S$someMethod2(final ScriptFunction function) {
            this.someMethod2 = function;
        }

        public ScriptFunction G$inChromeOnly2() {
            return this.inChromeOnly2;
        }

        public void S$inChromeOnly2(final ScriptFunction function) {
            this.inChromeOnly2 = function;
        }

        {
            final List<Property> list = new ArrayList<>(2);
            final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
            list.add(AccessorProperty.create("someMethod2", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$someMethod2", ScriptFunction.class),
                    virtualHandle("S$someMethod2", void.class, ScriptFunction.class)));
            if (browserFamily == CHROME) {
                list.add(AccessorProperty.create("inChromeOnly2", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$inChromeOnly2", ScriptFunction.class),
                    virtualHandle("S$inChromeOnly2", void.class, ScriptFunction.class)));
            }
            setMap(PropertyMap.newMap(list));
        }

        Prototype() {
            someMethod2 = ScriptFunction.createBuiltin("someMethod2",
                    staticHandle("someMethod2", String.class, Object.class));
            inChromeOnly2 = ScriptFunction.createBuiltin("inChromeOnly2",
                    staticHandle("inChromeOnly2", String.class, Object.class));
        }

        public String getClassName() {
            return "Host2";
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Prototype.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
