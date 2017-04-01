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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.gargoylesoftware.js.nashorn.SimpleObjectConstructor;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("Window")
public class MyWindow extends ScriptObject {

    private MyHTMLDocument document_;

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static MyWindow constructor(final boolean newObj, final Object self) {
        final MyWindow host = new MyWindow();
        host.setProto(Context.getGlobal().getPrototype(host.getClass()));
        return host;
    }

    @Getter
    public static MyHTMLDocument getDocument(final Object self) {
        MyWindow window = getWindow(self);
        if (window.document_ == null) {
            window.document_ = MyHTMLDocument.constructor(true, Global.instance());
        }
        return window.document_;
    }

    private static MyWindow getWindow(final Object self) {
        if (self instanceof Global) {
            MyWindow window = ((Global) self).getWindow();
            return window;
        }
        if (self instanceof MyWindow) {
            return (MyWindow) self;
        }
        return Global.instance().getWindow();
    }
    
    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(MyWindow.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class FunctionConstructor extends ScriptFunction {
        public FunctionConstructor() {
            super("Window", 
                    staticHandle("constructor", MyWindow.class, boolean.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    static final class Prototype extends PrototypeObject {
        public String getClassName() {
            return "Window";
        }
    }

    public static final class ObjectConstructor extends SimpleObjectConstructor {
        public ObjectConstructor() {
            super("Window");
        }
    }
}
