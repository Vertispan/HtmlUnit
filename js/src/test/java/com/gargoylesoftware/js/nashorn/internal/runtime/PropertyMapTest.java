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
package com.gargoylesoftware.js.nashorn.internal.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class PropertyMapTest {

    @Test
    public void iterator() {
        ScriptObject scriptObject = new ObjectConstructor();
        for (Iterator<?> it = scriptObject.getMap().iterator(); it.hasNext();) {
            it.next();
        }
    }

    public static final class ObjectConstructor extends ScriptObject {
        private ScriptFunction addEventListener;

        public ScriptFunction G$addEventListener() {
            return addEventListener;
        }

        public void S$addEventListener(final ScriptFunction function) {
            this.addEventListener = function;
        }

        {
            final List<Property> list = new ArrayList<>(1);
            list.add(AccessorProperty.create("addEventListener", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$addEventListener", ScriptFunction.class),
                    virtualHandle("S$addEventListener", void.class, ScriptFunction.class)));
            setMap(PropertyMap.newMap(list));
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(ObjectConstructor.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
