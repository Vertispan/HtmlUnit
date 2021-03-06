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
/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.gargoylesoftware.js.nashorn.internal.objects;

import static com.gargoylesoftware.js.nashorn.internal.codegen.CompilerConstants.specialCall;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Attribute;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Where;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.JSType;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.arrays.ArrayData;
import com.gargoylesoftware.js.nashorn.internal.runtime.arrays.TypedArrayData;

/**
 * Int32 array for the TypedArray extension
 */
@ScriptClass("Int32Array")
public final class NativeInt32Array extends ArrayBufferView {
    /**
     * The size in bytes of each element in the array.
     */
    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Property(attributes = Attribute.NOT_ENUMERABLE | Attribute.NOT_WRITABLE | Attribute.NOT_CONFIGURABLE, where = Where.CONSTRUCTOR)
    public static final int BYTES_PER_ELEMENT = 4;

    // initialized by nasgen
    @SuppressWarnings("unused")
    private static PropertyMap $nasgenmap$;

    private static final Factory FACTORY = new Factory(BYTES_PER_ELEMENT) {
        @Override
        public ArrayBufferView construct(final NativeArrayBuffer buffer, final int byteOffset, final int length) {
            return new NativeInt32Array(buffer, byteOffset, length);
        }

        @Override
        public Int32ArrayData createArrayData(final ByteBuffer nb, final int start, final int length) {
            return new Int32ArrayData(nb.asIntBuffer(), start, length);
        }

        @Override
        public String getClassName() {
            return "Int32Array";
        }
    };

    private static final class Int32ArrayData extends TypedArrayData<IntBuffer> {

        private static final MethodHandle GET_ELEM = specialCall(MethodHandles.lookup(), Int32ArrayData.class, "getElem", int.class, int.class).methodHandle();
        private static final MethodHandle SET_ELEM = specialCall(MethodHandles.lookup(), Int32ArrayData.class, "setElem", void.class, int.class, int.class).methodHandle();

        private Int32ArrayData(final IntBuffer nb, final int start, final int end) {
            super(((IntBuffer)nb.position(start).limit(end)).slice(), end - start);
        }

        @Override
        protected MethodHandle getGetElem() {
            return GET_ELEM;
        }

        @Override
        protected MethodHandle getSetElem() {
            return SET_ELEM;
        }

        private int getElem(final int index) {
            try {
                return nb.get(index);
            } catch (final IndexOutOfBoundsException e) {
                throw new ClassCastException(); //force relink - this works for unoptimistic too
            }
        }

        private void setElem(final int index, final int elem) {
            try {
                if (index < nb.limit()) {
                    nb.put(index, elem);
                }
            } catch (final IndexOutOfBoundsException e) {
                throw new ClassCastException();
            }
        }

        @Override
        public Class<?> getElementType() {
            return int.class;
        }

        @Override
        public Class<?> getBoxedElementType() {
            return Integer.class;
        }

        @Override
        public int getInt(final int index) {
            return getElem(index);
        }

        @Override
        public int getIntOptimistic(final int index, final int programPoint) {
            return getElem(index);
        }

        @Override
        public double getDouble(final int index) {
            return getInt(index);
        }

        @Override
        public double getDoubleOptimistic(final int index, final int programPoint) {
            return getElem(index);
        }

        @Override
        public Object getObject(final int index) {
            return getInt(index);
        }

        @Override
        public ArrayData set(final int index, final Object value, final boolean strict) {
            return set(index, JSType.toInt32(value), strict);
        }

        @Override
        public ArrayData set(final int index, final int value, final boolean strict) {
            setElem(index, value);
            return this;
        }

        @Override
        public ArrayData set(final int index, final double value, final boolean strict) {
            return set(index, (int)value, strict);
        }
    }

    /**
     * Constructor
     *
     * @param newObj is this typed array instantiated with the new operator
     * @param self   self reference
     * @param args   args
     *
     * @return new typed array
     */
    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor(arity = 1)
    public static NativeInt32Array constructor(final boolean newObj, final Object self, final Object... args) {
        return (NativeInt32Array)constructorImpl(newObj, args, FACTORY);
    }

    NativeInt32Array(final NativeArrayBuffer buffer, final int byteOffset, final int length) {
        super(buffer, byteOffset, length);
    }

    @Override
    protected Factory factory() {
        return FACTORY;
    }

    /**
     * Set values
     * @param self   self reference
     * @param array  multiple values of array's type to set
     * @param offset optional start index, interpreted  0 if undefined
     * @return undefined
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    protected static Object set(final Object self, final Object array, final Object offset) {
        return ArrayBufferView.setImpl(self, array, offset);
    }

    /**
     * Returns a new TypedArray view of the ArrayBuffer store for this TypedArray,
     * referencing the elements at begin, inclusive, up to end, exclusive. If either
     * begin or end is negative, it refers to an index from the end of the array,
     * as opposed to from the beginning.
     * <p>
     * If end is unspecified, the subarray contains all elements from begin to the end
     * of the TypedArray. The range specified by the begin and end values is clamped to
     * the valid index range for the current array. If the computed length of the new
     * TypedArray would be negative, it is clamped to zero.
     * <p>
     * The returned TypedArray will be of the same type as the array on which this
     * method is invoked.
     *
     * @param self self reference
     * @param begin begin position
     * @param end end position
     *
     * @return sub array
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    protected static NativeInt32Array subarray(final Object self, final Object begin, final Object end) {
        return (NativeInt32Array)ArrayBufferView.subarrayImpl(self, begin, end);
    }

    @Override
    protected ScriptObject getPrototype(final Global global) {
        return global.getInt32ArrayPrototype();
    }

    static {
            final List<Property> list = Collections.emptyList();
            $nasgenmap$ = PropertyMap.newMap(list);
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(NativeInt32Array.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static final class Constructor extends ScriptFunction {
        private static final PropertyMap $nasgenmap$;

        static {
            final List<Property> list = new ArrayList<>(1);
            list.add(AccessorProperty.create("BYTES_PER_ELEMENT", Property.NOT_WRITABLE | Property.NOT_ENUMERABLE | Property.NOT_CONFIGURABLE, 
                    virtualHandle("G$BYTES_PER_ELEMENT", int.class), null));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        public int G$BYTES_PER_ELEMENT() {
            return BYTES_PER_ELEMENT;
        }

        Constructor() {
            super("Int32Array", 
                    staticHandle("constructor", NativeInt32Array.class, boolean.class, Object.class, Object[].class),
                    $nasgenmap$, null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
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

    static final class Prototype extends PrototypeObject {
        private ScriptFunction set;
        private ScriptFunction subarray;
        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$set() {
            return this.set;
        }

        public void S$set(final ScriptFunction function) {
            this.set = function;
        }

        public ScriptFunction G$subarray() {
            return this.subarray;
        }

        public void S$subarray(final ScriptFunction function) {
            this.subarray = function;
        }

        static {
            final List<Property> list = new ArrayList<>(2);
            list.add(AccessorProperty.create("set", Property.NOT_ENUMERABLE, 
                    virtualHandle("G$set", ScriptFunction.class),
                    virtualHandle("S$set", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("subarray", Property.NOT_ENUMERABLE, 
                    virtualHandle("G$subarray", ScriptFunction.class),
                    virtualHandle("S$subarray", void.class, ScriptFunction.class)));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
            super($nasgenmap$);
            set = ScriptFunction.createBuiltin("set",
                    staticHandle("set", Object.class, Object.class, Object.class, Object.class));
            subarray = ScriptFunction.createBuiltin("subarray",
                    staticHandle("subarray", NativeInt32Array.class, Object.class, Object.class, Object.class));
        }

        public String getClassName() {
            return "Int32Array";
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
