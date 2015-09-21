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
 * Copyright (c) 2002-2015 Gargoyle Software Inc.
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
