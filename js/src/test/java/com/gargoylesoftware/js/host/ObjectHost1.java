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
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Where;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("ObjectHost1")
public class ObjectHost1 extends ScriptObject {

    @Function(where = Where.CONSTRUCTOR)
    public static String parentMethod(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Function(browsers = @WebBrowser(CHROME), where = Where.CONSTRUCTOR)
    public static String inChromeOnly(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Getter(browsers = {@WebBrowser(value = IE, minVersion = 11), @WebBrowser(CHROME) }, where = Where.CONSTRUCTOR)
    public static int length(final Object self) {
        return Browser.getCurrent().getFamily() == CHROME ? 1 : 2;
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(ObjectHost1.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptObject {
        private ScriptFunction parentMethod;
        private ScriptFunction inChromeOnly;

        public ScriptFunction G$parentMethod() {
            return this.parentMethod;
        }

        public void S$parentMethod(final ScriptFunction function) {
            this.parentMethod = function;
        }

        public ScriptFunction G$inChromeOnly() {
            return this.inChromeOnly;
        }

        public void S$inChromeOnly(final ScriptFunction function) {
            this.inChromeOnly = function;
        }

        {
            final List<Property> list = new ArrayList<>(3);
            final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
            final int browserVersion = Browser.getCurrent().getVersion();
            list.add(AccessorProperty.create("parentMethod", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$parentMethod", ScriptFunction.class),
                    virtualHandle("S$parentMethod", void.class, ScriptFunction.class)));
            if (browserFamily == CHROME) {
                list.add(AccessorProperty.create("inChromeOnly", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$inChromeOnly", ScriptFunction.class),
                    virtualHandle("S$inChromeOnly", void.class, ScriptFunction.class)));
            }
            if ((browserFamily == IE && browserVersion >= 11) || browserFamily == CHROME) {
                list.add(AccessorProperty.create("length", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    staticHandle("length", int.class, Object.class),
                    null));
            }
            setMap(PropertyMap.newMap(list));
        }

        public Constructor() {
            parentMethod = ScriptFunction.createBuiltin("parentMethod",
                    staticHandle("parentMethod", String.class, Object.class));
            inChromeOnly = ScriptFunction.createBuiltin("inChromeOnly",
                    staticHandle("inChromeOnly", String.class, Object.class));
        }

        public String getClassName() {
            return "ObjectHost1";
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Constructor.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
