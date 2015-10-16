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

import com.gargoylesoftware.js.nashorn.ScriptUtils;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.WebBrowser;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("FunctionHost2")
public class FunctionHost2 extends ScriptObject {

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static FunctionHost2 constructor(final boolean newObj, final Object self) {
        final FunctionHost2 host = new FunctionHost2();
        host.setProto(Context.getGlobal().getPrototype(host.getClass()));
        return host;
    }

    @Function
    public static String someMethod2(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Function(@WebBrowser(CHROME))
    public static String inChromeOnly2(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Getter({@WebBrowser(value = IE, minVersion = 11), @WebBrowser(CHROME) })
    public static int getLength2(final Object self) {
        return Browser.getCurrent().getFamily() == CHROME ? 1 : 2;
    }


    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(FunctionHost2.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptFunction {
        public Constructor() {
            super("FunctionHost2", 
                    staticHandle("constructor", FunctionHost2.class, boolean.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    public static final class Prototype extends PrototypeObject {
        public ScriptFunction someMethod2;
        public ScriptFunction inChromeOnly2;

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

        Prototype() {
            ScriptUtils.initialize(this);
        }

        public String getClassName() {
            return "FunctionHost2";
        }
    }
}
