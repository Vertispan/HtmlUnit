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

import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;

/**
 * Base class for HtmlUnit prototype objects.
 *
 * The main functions is to store all getter and setter of {@link ScriptFunction}s.
 */
public class SimplePrototypeObject extends PrototypeObject {

    private final String className_;
    private Map<String, ScriptFunction> map_ = new HashMap<>();

    protected SimplePrototypeObject(final String className) {
        className_ = className;
        ScriptUtils.initialize(this);
    }

    /**
     * Returns the {@code ScriptFunction} with the specified {@code functionName}.
     * @param functionName the function name
     * @return the {@code ScriptFunction}
     */
    public ScriptFunction getScriptFunction(final String functionName) {
        return map_.get(functionName);
    }

    /**
     * Sets the {@code ScriptFunction}.
     * @param function the the {@code ScriptFunction}
     * @param functionName the function name
     */
    public void setScriptFunction(final ScriptFunction function, final String functionName) {
        map_.put(functionName, function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        return className_;
    }
}
