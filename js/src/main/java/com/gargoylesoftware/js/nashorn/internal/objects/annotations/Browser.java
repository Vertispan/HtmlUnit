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
package com.gargoylesoftware.js.nashorn.internal.objects.annotations;

/**
 * Represents a real browser.
 */
public class Browser {

    private static final ThreadLocal<Browser> current_ = new ThreadLocal<>();

    private final BrowserFamily family_;
    private final int version_;

    public Browser(final BrowserFamily family, final int version) {
        family_ = family;
        version_ = version;
    }

    public BrowserFamily getFamily() {
        return family_;
    }

    public int getVersion() {
        return version_;
    }

    /**
     * Returns the currently used {@code Browser}.
     * @return the currently used {@code Browser}
     */
    public static Browser getCurrent() {
        return current_.get();
    }

    /**
     * Sets the currently used {@code Browser}.
     * @param browser the browser
     */
    public static void setCurrent(final Browser browser) {
        current_.set(browser);
    }
}
