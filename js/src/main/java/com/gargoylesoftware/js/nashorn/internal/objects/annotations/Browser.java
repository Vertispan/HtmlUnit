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

    private static final ThreadLocal<WebBrowser> current_ = new ThreadLocal<>();

    private final WebBrowser family_;

    public Browser(final WebBrowser browser) {
        family_ = browser;
    }

    public WebBrowser getFamily() {
        return family_;
    }

    /**
     * Returns the currently used {@code WebBrowser}.
     * @return the currently used {@code WebBrowser}
     */
    public static WebBrowser getCurrent() {
        return current_.get();
    }

    /**
     * Sets the currently used {@code Browser}.
     * @param browser the browser
     */
    public static void setCurrent(final WebBrowser browser) {
        current_.set(browser);
    }
}
