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

import com.gargoylesoftware.js.nashorn.ScriptUtils;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Getter;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.WebBrowser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Where;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("ObjectHost2")
public class ObjectHost2 extends ScriptObject {

    @Function(where = Where.CONSTRUCTOR)
    public static String childMethod(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Function(browsers = @WebBrowser(CHROME), where = Where.CONSTRUCTOR)
    public static String inChromeOnly2(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    @Getter(browsers = {@WebBrowser(value = IE, minVersion = 11), @WebBrowser(CHROME) }, where = Where.CONSTRUCTOR)
    public static int length2(final Object self) {
        return Browser.getCurrent().getFamily() == CHROME ? 1 : 2;
    }

    public static final class Constructor extends ScriptObject {
        public ScriptFunction childMethod;
        public ScriptFunction inChromeOnly2;

        public ScriptFunction G$childMethod() {
            return this.childMethod;
        }

        public void S$childMethod(final ScriptFunction function) {
            this.childMethod = function;
        }

        public ScriptFunction G$inChromeOnly2() {
            return this.inChromeOnly2;
        }

        public void S$inChromeOnly2(final ScriptFunction function) {
            this.inChromeOnly2 = function;
        }

        Constructor() {
            ScriptUtils.initialize(this);
        }

        public String getClassName() {
            return "ObjectHost2";
        }
    }
}
