/*
 * Copyright (c) 2002-2017 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.background;

import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;

import net.sourceforge.htmlunit.corejs.javascript.Function;

/**
 * A {@link JavaScriptJob} created from a {@link Function} object.
 *
 * @author Brad Clarke
 * @author Ahmed Ashour
 */
class JavaScriptFunctionJob2 extends JavaScriptExecutionJob {

    /** The JavaScript code to execute. */
    private final ScriptFunction function_;

    /**
     * Creates a new JavaScript execution job, where the JavaScript code to execute is a function.
     * @param initialDelay the initial amount of time to wait before executing this job
     * @param period the amount of time to wait between executions of this job (may be {@code null})
     * @param label the label for the job
     * @param window the window to which the job belongs
     * @param function the JavaScript code to execute
     */
    JavaScriptFunctionJob2(final int initialDelay, final Integer period, final String label,
        final WebWindow window, final ScriptFunction function) {
        super(initialDelay, period, label, window);
        function_ = function;
    }

    /** {@inheritDoc} */
    @Override
    protected void runJavaScript(final HtmlPage page) {
        final HtmlElement doc = page.getDocumentElement();
        final Global oldGlobal = Context.getGlobal();
        final Global global = page.getEnclosingWindow().getScriptableObject();
        try {
            Context.setGlobal(global);
            page.executeJavaScriptFunction(function_, global, new Object[0], doc);
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

}
