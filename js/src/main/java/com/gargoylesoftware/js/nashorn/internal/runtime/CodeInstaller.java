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

package com.gargoylesoftware.js.nashorn.internal.runtime;

import java.util.Collection;
import java.util.Map;

import com.gargoylesoftware.js.nashorn.internal.codegen.ClassEmitter;

/**
 * Interface for installing classes passed to the compiler.
 * As only the code generating package (i.e. Context) knows about
 * the ScriptLoader and it would be a security hazard otherwise
 * the Compiler is given an installation interface for its code.
 * <p>
 * The compiler still retains most of the state around code emission
 * and management internally, so this is to avoid passing around any
 * logic that isn't directly related to installing a class
 * @param <T> owner class type for this code installer
 *
 */
public interface CodeInstaller {
    /**
     * Return the {@link Context} associated with this code installer.
     * @return the context.
     */
    public Context getContext();

    /**
     * Install a class.
     * @param className name of the class with / separation
     * @param bytecode  bytecode
     * @return the installed class
     */
    public Class<?> install(final String className, final byte[] bytecode);

    /**
     * Initialize already installed classes.
     * @param classes the class to initialize
     * @param source the source object for the classes
     * @param constants the runtime constants for the classes
     */
    public void initialize(final Collection<Class<?>> classes, final Source source, final Object[] constants);

    /**
     * Verify generated bytecode before emission. This is called back from the
     * {@link ClassEmitter} or the {@link Compiler}. If the "--verify-code" parameter
     * hasn't been given, this is a nop
     *
     * @param code bytecode to verify
     */
    public void verify(final byte[] code);

    /**
     * Get next unique script id
     * @return unique script id
     */
    public long getUniqueScriptId();

    /**
     * Store a compiled script for later reuse
     *
     * @param cacheKey key to use in cache
     * @param source the script source
     * @param mainClassName the main class name
     * @param classBytes map of class names to class bytes
     * @param initializers compilation id -&gt; FunctionInitializer map
     * @param constants constants array
     * @param compilationId compilation id
     */
    public void storeScript(final String cacheKey, final Source source, final String mainClassName, final Map<String, byte[]> classBytes,
            final Map<Integer, FunctionInitializer> initializers, final Object[] constants, final int compilationId);

    /**
     * Load a previously compiled script
     * @param source the script source
     * @param functionKey the function id and signature
     * @return compiled script data
     */
    public StoredScript loadScript(Source source, String functionKey);

    /**
     * Returns a code installer {@code #isCompatibleWith(CodeInstaller) compatible with} this installer, but
     * is suitable for on-demand compilations. Can return itself if it is itself suitable.
     * @return a compatible code installer suitable for on-demand compilations.
     */
    public CodeInstaller getOnDemandCompilationInstaller();

    /**
     * Returns a code installer {@code #isCompatibleWith(CodeInstaller) compatible with} this installer, but
     * is suitable for installation of multiple classes that reference each other by name. Should be used when
     * a compilation job produces multiple compilation units. Can return itself if it is itself suitable.
     * @return a compatible code installer suitable for installation of multiple classes.
     */
    public CodeInstaller getMultiClassCodeInstaller();

    /**
     * Returns true if this code installer is compatible with the other code installer. Compatibility is expected to be
     * an equivalence relation, and installers are supposed to be compatible with those they create using
     * {@link #getOnDemandCompilationInstaller()}.
     * @param other the other code installer tested for compatibility with this code installer.
     * @return true if this code installer is compatible with the other code installer.
     */
    public boolean isCompatibleWith(CodeInstaller other);

}
