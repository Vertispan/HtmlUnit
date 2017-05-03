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
 * An annotation to specify a browser.
 */
public enum SupportedBrowser {

    /** Latest version of Chrome. */
    CHROME,

    /** Internet Explorer 11. */
    IE,

    /** Edge. */
    EDGE,

    /** All versions of Firefox. */
    FF,

    /** Firefox 45. */
    FF45,

    /** Firefox 52. */
    FF52

}
