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
package com.gargoylesoftware.htmlunit.javascript.host.html;

import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;

import com.gargoylesoftware.htmlunit.html.HtmlSlot;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;

/**
 * A JavaScript object for {@code HTMLSlotElement}.
 *
 * @author Ahmed Ashour
 */
@JsxClass(domClass = HtmlSlot.class, browsers = CHROME)
public class HTMLSlotElement extends HTMLElement {

    /**
     * Creates an instance.
     */
    @JsxConstructor
    public HTMLSlotElement() {
    }
}
