/*
 * Copyright (c) 2016 Gargoyle Software Inc.
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
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
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
        final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
        final int browserVersion = Browser.getCurrent().getVersion();
        Class<?> enclosingClass = scriptObject.getClass().getEnclosingClass();
        if (enclosingClass == null) {
            enclosingClass = scriptObject.getClass();
        }
        final List<Property> list = new ArrayList<>();

        final Method[] allMethods = getAllMethods(scriptObject, enclosingClass);
        final Field[] allFields = enclosingClass.getDeclaredFields();
        final Map<String, Method> setters = new HashMap<>();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (final Method method : allMethods) {
            for (final Function function : method.getAnnotationsByType(Function.class)) {
                if (isSupported(scriptObject, function.where(), function.value(), browserFamily, browserVersion)) {
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
                if (isSupported(scriptObject, setter.where(), setter.value(), browserFamily, browserVersion)) {
                    String fieldName = method.getName().substring(3);
                    fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                    setters.put(fieldName, method);
                }
            }
        }
        for (final Field field : allFields) {
            for (final com.gargoylesoftware.js.nashorn.internal.objects.annotations.Property property
                    : field.getAnnotationsByType(
                            com.gargoylesoftware.js.nashorn.internal.objects.annotations.Property.class)) {
                if (isSupported(scriptObject, property.where(), property.value(), browserFamily, browserVersion)) {
                    final String propertyName = field.getName();
                    list.add(AccessorProperty.create(propertyName, property.attributes(), 
                            virtualHandle(scriptObject.getClass(), "G$" + propertyName,
                                    int.class), null));
                }
            }
        }
        for (final Method method : allMethods) {
            for (final Getter getter : method.getAnnotationsByType(Getter.class)) {
                if (isSupported(scriptObject, getter.where(), getter.value(), browserFamily, browserVersion)) {
                    try {
                        String fieldName = method.getName().substring(3);
                        fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);

                        final MethodHandle setter;
                        final Method setterMethod = setters.get(fieldName);
                        if (setterMethod != null) {
                            setter = lookup.unreflect(setterMethod);
                        }
                        else if (browserFamily != BrowserFamily.IE) {
                            setter = Lookup.EMPTY_SETTER;
                        }
                        else {
                            setter = MethodHandles.insertArguments(SETTER_, 2, fieldName);
                        }

                        list.add(AccessorProperty.create(fieldName, getter.attributes(), 
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
     * For {@code instances}, return all declared methods including in super classes, which is needed
     * for setting the {@code getter} and {@code setter} at {@code Where#INSTANCE}.
     */
    private static Method[] getAllMethods(final ScriptObject scriptObject, final Class<?> enclosingClass) {
        if (scriptObject.getClass().getEnclosingClass() != null) {
            return enclosingClass.getDeclaredMethods();
        }
        final List<Method> list = new ArrayList<>();
        for (Class<?> klass = enclosingClass; klass != null; klass = klass.getSuperclass()) {
            Collections.addAll(list, klass.getDeclaredMethods());
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

    private static boolean isSupported(final ScriptObject scriptObject, final Where where, final WebBrowser[] browsers, final BrowserFamily expectedBrowserFamily,
            final int expectedBrowserVersion) {
        
        if (where == Where.PROTOTYPE
                && (scriptObject.getClass().getEnclosingClass() == null || scriptObject instanceof ScriptFunction)
                || where != Where.PROTOTYPE && scriptObject instanceof PrototypeObject
                || where == Where.CONSTRUCTOR && scriptObject.getClass().getEnclosingClass() == null
                || where == Where.INSTANCE && scriptObject.getClass().getEnclosingClass() != null) {
            return false;
        }

        for (final WebBrowser browser : browsers) {
            if (browser.value() == expectedBrowserFamily
                    && browser.minVersion() <= expectedBrowserVersion
                    && browser.maxVersion() >= expectedBrowserVersion) {
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
        if (self instanceof Global) {
            ((Global) self).getWindow().addOwnProperty(name, Property.WRITABLE_ENUMERABLE_CONFIGURABLE, value);
        }
        if (self == Global.instance().getWindow()) {
            Global.instance().addOwnProperty(name, Property.WRITABLE_ENUMERABLE_CONFIGURABLE, value);
        }
    }

}
