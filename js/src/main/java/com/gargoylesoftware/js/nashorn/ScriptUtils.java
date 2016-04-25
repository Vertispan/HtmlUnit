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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void initialize(final ScriptObject scriptObject) {
        //final boolean isPrototype = scriptObject instanceof PrototypeObject;
        final BrowserFamily browserFamily = Browser.getCurrent().getFamily();
        final int browserVersion = Browser.getCurrent().getVersion();
        Class<?> enclosingClass = scriptObject.getClass().getEnclosingClass();
        if (enclosingClass == null) {
            enclosingClass = scriptObject.getClass();
        }
        final List<Property> list = new ArrayList<>(2);

        final Method[] allMethods = enclosingClass.getDeclaredMethods();
        final Field[] allFields = enclosingClass.getDeclaredFields();
        final Map<String, Method> setters = new HashMap<>();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (final Method method : allMethods) {
            for (final Function function : method.getAnnotationsByType(Function.class)) {
                if (isSupported(scriptObject, function.where(), function.value(), browserFamily, browserVersion)) {
                    final String methodName = method.getName();
                    list.add(AccessorProperty.create(methodName, function.attributes(), 
                            virtualHandle(scriptObject.getClass(), "G$" + methodName,
                                    ScriptFunction.class),
                            virtualHandle(scriptObject.getClass(), "S$" + methodName,
                                    void.class, ScriptFunction.class)));
                    try {
                        final ScriptFunction scriptFunction =
                                ScriptFunction.createBuiltin(method.getName(), lookup.unreflect(method));
                        final MethodHandle handle =
                                lookup.findSetter(scriptObject.getClass(), methodName, ScriptFunction.class);
                        handle.invoke(scriptObject, scriptFunction);
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
                    MethodHandle setter = null;
                    String fieldName = method.getName().substring(3);
                    fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                    final Method setterMethod = setters.get(fieldName);

                    try {
                        if (setterMethod != null) {
                            setter = lookup.unreflect(setterMethod);
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
        
        if (where == Where.PROTOTYPE && !(scriptObject instanceof PrototypeObject)
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

}
