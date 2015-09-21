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

@ScriptClass("FunctionHost1")
public class FunctionHost1 extends ScriptObject {

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static FunctionHost1 constructor(final boolean newObj, final Object self) {
        final FunctionHost1 host = new FunctionHost1();
        host.setProto(Context.getGlobal().getPrototype(host.getClass()));
        return host;
    }

    @Function
    public static String someMethod(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Function(browsers = @WebBrowser(CHROME))
    public static String inChromeOnly(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Getter(browsers = {@WebBrowser(value = IE, minVersion = 11), @WebBrowser(CHROME) })
    public static int length(final Object self) {
        return Browser.getCurrent().getFamily() == CHROME ? 1 : 2;
    }

    {
        final List<Property> list = new ArrayList<>(1);
        final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
        final int browserVersion = Browser.getCurrent().getVersion();
        if ((browserFamily == IE && browserVersion >= 11) || browserFamily == CHROME) {
            list.add(AccessorProperty.create("length", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    staticHandle("length", int.class, Object.class),
                    null));
        }
        setMap(PropertyMap.newMap(list));
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(FunctionHost1.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptFunction {
        public Constructor() {
            super("FunctionHost1", 
                    staticHandle("constructor", FunctionHost1.class, boolean.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction someMethod;
        private ScriptFunction inChromeOnly;

        public ScriptFunction G$someMethod() {
            return this.someMethod;
        }

        public void S$someMethod(final ScriptFunction function) {
            this.someMethod = function;
        }

        public ScriptFunction G$inChromeOnly() {
            return this.inChromeOnly;
        }

        public void S$inChromeOnly(final ScriptFunction function) {
            this.inChromeOnly = function;
        }

        {
            final List<Property> list = new ArrayList<>(2);
            final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
            list.add(AccessorProperty.create("someMethod", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$someMethod", ScriptFunction.class),
                    virtualHandle("S$someMethod", void.class, ScriptFunction.class)));
            if (browserFamily == CHROME) {
                list.add(AccessorProperty.create("inChromeOnly", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$inChromeOnly", ScriptFunction.class),
                    virtualHandle("S$inChromeOnly", void.class, ScriptFunction.class)));
            }
            setMap(PropertyMap.newMap(list));
        }

        Prototype() {
            someMethod = ScriptFunction.createBuiltin("someMethod",
                    staticHandle("someMethod", String.class, Object.class));
            inChromeOnly = ScriptFunction.createBuiltin("inChromeOnly",
                    staticHandle("inChromeOnly", String.class, Object.class));
        }

        public String getClassName() {
            return "FunctionHost1";
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
