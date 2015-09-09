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
package jdk2.nashorn.internal.objects.annotations;

/**
 * Represents a real browser.
 *
 * @version $Revision$
 * @author Ahmed Ashour
 */
public class Browser {

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
}
