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
package com.gargoylesoftware.js.nashorn;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.js.nashorn.internal.lookup.Lookup;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Setter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.WebBrowser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Where;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

public class ScriptUtils {

    private static final MethodHandle SIMPLE_PROTOTYPE_GETTER_ = virtualHandle(SimplePrototypeObject.class, "getScriptFunction",
            ScriptFunction.class, String.class);
    private static final MethodHandle SIMPLE_PROTOTYPE_SETTER_ = virtualHandle(SimplePrototypeObject.class, "setScriptFunction",
            void.class, ScriptFunction.class, String.class);

    private static final MethodHandle SIMPLE_CONSTRUCTOR_GETTER_ = virtualHandle(SimpleObjectConstructor.class, "getScriptFunction",
            ScriptFunction.class, String.class);
    private static final MethodHandle SIMPLE_CONSTRUCTOR_SETTER_ = virtualHandle(SimpleObjectConstructor.class, "setScriptFunction",
            void.class, ScriptFunction.class, String.class);

    private static final MethodHandle SETTER_ = staticHandle(ScriptUtils.class, "set",
            void.class, ScriptObject.class, Object.class, String.class);

    public static void initialize(final ScriptObject scriptObject) {
        final WebBrowser webBrowser = Browser.getCurrent();
        final Class<?> scriptClass = getScriptClass(scriptObject);
        Class<?> enclosingClass = scriptClass.getEnclosingClass();
        if (enclosingClass == null) {
            enclosingClass = scriptClass;
        }
        final List<Property> list = new ArrayList<>();

        final Method[] allMethods = getAllMethods(scriptClass, enclosingClass);
        final Field[] allFields = enclosingClass.getDeclaredFields();
        final Map<String, Method> setters = new HashMap<>();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (final Method method : allMethods) {
            for (final Function function : method.getAnnotationsByType(Function.class)) {
                if (isSupported(scriptObject, function.where(), function.value(), webBrowser)) {
                    final String functionName;
                    if (function.name().isEmpty()) {
                        functionName = method.getName();
                    }
                    else {
                        functionName = function.name();
                    }
                    final MethodHandle getter;
                    final MethodHandle setter;
                    if (scriptObject instanceof SimplePrototypeObject) {
                        getter = MethodHandles.insertArguments(SIMPLE_PROTOTYPE_GETTER_, 1, functionName);
                        setter = MethodHandles.insertArguments(SIMPLE_PROTOTYPE_SETTER_, 2, functionName);
                    }
                    else {
                        getter = MethodHandles.insertArguments(SIMPLE_CONSTRUCTOR_GETTER_, 1, functionName);
                        setter = MethodHandles.insertArguments(SIMPLE_CONSTRUCTOR_SETTER_, 2, functionName);
                    }

                    int attributes = function.attributes();
                    attributes |= Property.NOT_ENUMERABLE;

                    list.add(AccessorProperty.create(functionName, attributes, getter, setter));

                    try {
                        final ScriptFunction scriptFunction =
                                ScriptFunction.createBuiltin(method.getName(), lookup.unreflect(method));
                        setter.invoke(scriptObject, scriptFunction);
                    }
                    catch(final Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
            for (final Setter setter : method.getAnnotationsByType(Setter.class)) {
                if (isSupported(scriptObject, setter.where(), setter.value(), webBrowser)) {
                    String name;
                    if (setter.name().isEmpty()) {
                        name = method.getName().substring(3);
                        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    }
                    else {
                        name = setter.name();
                    }
                    setters.put(name, method);
                }
            }
        }
        for (final Field field : allFields) {
            for (final com.gargoylesoftware.js.nashorn.internal.objects.annotations.Property property
                    : field.getAnnotationsByType(
                            com.gargoylesoftware.js.nashorn.internal.objects.annotations.Property.class)) {
                if (isSupported(scriptObject, property.where(), property.value(), webBrowser)) {
                    final String propertyName = field.getName();
                    list.add(AccessorProperty.create(propertyName, property.attributes(), 
                            virtualHandle(scriptObject.getClass(), "G$" + propertyName,
                                    int.class), null));
                }
            }
        }
        for (final Method method : allMethods) {
            for (final Getter getter : method.getAnnotationsByType(Getter.class)) {
                if (isSupported(scriptObject, getter.where(), getter.value(), webBrowser)) {
                    try {
                        String name;
                        if (getter.name().isEmpty()) {
                            name = method.getName();
                            name = name.substring(name.startsWith("is") ? 2 : 3);
                            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                        }
                        else {
                            name = getter.name();
                        }

                        final MethodHandle setter;
                        final Method setterMethod = setters.get(name);
                        if (setterMethod != null) {
                            setter = lookup.unreflect(setterMethod);
                        }
                        else if (webBrowser != WebBrowser.IE) {
                            setter = Lookup.EMPTY_SETTER;
                        }
                        else {
                            setter = MethodHandles.insertArguments(SETTER_, 2, name);
                        }

                        list.add(AccessorProperty.create(name, getter.attributes(), 
                                lookup.unreflect(method),
                                setter));
                    }
                    catch (final IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        scriptObject.setMap(PropertyMap.newMap(list));
    }

    /**
     * Returns the script class, handling anonymous ones. 
     */
    private static Class<?> getScriptClass(final Object scriptObject) {
        Class<?> scriptClass = scriptObject.getClass();
        final String name = scriptClass.getName();
        if (name.indexOf('$') != -1) {
            final char ch = name.charAt(name.length() - 1);
            if (ch >= '0' && ch <= '9') {
                scriptClass = scriptClass.getSuperclass();
            }
        }
        return scriptClass;
    }

    /**
     * For {@code instances}, return all declared methods including in super classes, which is needed
     * for setting the {@code getter} and {@code setter} at {@code Where#INSTANCE}.
     */
    private static Method[] getAllMethods(final Class<?> scriptClass, final Class<?> enclosingClass) {
        final boolean nullProto = enclosingClass.getAnnotation(ScriptClass.class).nullProto();
        final List<Method> list = new ArrayList<>();
        boolean foundSuperScriptClass = false;
        for (Class<?> klass = enclosingClass; klass != null; klass = klass.getSuperclass()) {
            if (klass == ScriptObject.class) {
                break;
            }
            final boolean isScriptClass = klass.getAnnotation(ScriptClass.class) != null;
            if (klass != enclosingClass && isScriptClass && !nullProto) {
                foundSuperScriptClass = true;
            }
            final Method[] declaredMethods = klass.getDeclaredMethods();
            if (!foundSuperScriptClass) {
                Collections.addAll(list, klass.getDeclaredMethods());
            }
            else {
                for (final Method method : declaredMethods) {
                    if (method.getAnnotation(Getter.class) != null
                            || method.getAnnotation(Setter.class) != null) {
                        list.add(method);
                    }
                }
            }
        }
        return list.toArray(new Method[list.size()]);
    }

    private static MethodHandle virtualHandle(final Class<?> klass, final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findVirtual(klass, name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isSupported(final ScriptObject scriptObject, final Where where, final WebBrowser[] browsers,
            final WebBrowser expectedBrowser) {
        final Class<?> scriptClass = getScriptClass(scriptObject);
        if (where == Where.PROTOTYPE
                && (scriptClass.getEnclosingClass() == null || scriptObject instanceof ScriptFunction)
                || where != Where.PROTOTYPE && scriptObject instanceof PrototypeObject
                || where == Where.CONSTRUCTOR && scriptClass.getEnclosingClass() == null
                || where == Where.INSTANCE && scriptClass.getEnclosingClass() != null) {
            return false;
        }

        for (final WebBrowser browser : browsers) {
            if (browser == expectedBrowser) {
                return true;
            }
        }
        return false;
    }

    private static MethodHandle staticHandle(final Class<?> klass, final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(klass, name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sets property of an object.
     * @param self this object
     * @param value the value
     * @param name the property name
     */
    public static void set(final ScriptObject self, final Object value, final String name) {
        self.addOwnProperty(name, Property.WRITABLE_ENUMERABLE_CONFIGURABLE, value);
    }

}
