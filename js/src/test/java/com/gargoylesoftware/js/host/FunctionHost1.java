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
package com.gargoylesoftware.js.host;

import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.SupportedBrowser.CHROME;
import static com.gargoylesoftware.js.nashorn.internal.objects.annotations.SupportedBrowser.IE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.gargoylesoftware.js.nashorn.ScriptUtils;
import com.gargoylesoftware.js.nashorn.SimplePrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.FindProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("FunctionHost1")
public class FunctionHost1 extends ScriptObject {

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static FunctionHost1 constructor(final boolean newObj, final Object self) {
        final FunctionHost1 host = new FunctionHost1();
        host.setProto(Context.getGlobal().getPrototype(host.getClass()));
        ScriptUtils.initialize(host);
        return host;
    }

    @Function
    public static String someMethod(final Object self) {
        return Browser.getCurrent().name();
    }

    @Function(CHROME)
    public static String inChromeOnly(final Object self) {
        return Browser.getCurrent().name();
    }

    @Getter({IE, CHROME})
    public static int getLength(final Object self) {
        return Browser.getCurrent() == CHROME ? 1 : 2;
    }

    @Getter
    public static String getMySelf(final Object self) {
        return self.getClass().getName();
    }

    @Override
    public FindProperty findProperty(final String key, final boolean deep, final ScriptObject start) {
        if ("something".equals(key)) {
            start.addOwnProperty(key, Property.WRITABLE_ENUMERABLE_CONFIGURABLE, "new thing");
        }
        return super.findProperty(key, deep, start);
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

    public static final class FunctionConstructor extends ScriptFunction {
        public FunctionConstructor() {
            super("FunctionHost1", 
                    staticHandle("constructor", FunctionHost1.class, boolean.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    public static final class Prototype extends SimplePrototypeObject {
        Prototype() {
            super("FunctionHost1");
        }
    }
}
