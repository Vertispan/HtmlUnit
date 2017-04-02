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
package com.gargoylesoftware.htmlunit.javascript;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.BackgroundJavaScriptFactory;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptExecutor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JavaScriptConfiguration;
import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngine;
import com.gargoylesoftware.js.nashorn.api.scripting.NashornScriptEngineFactory;
import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.BrowserFamily;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ClassConstructor;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.WebBrowser;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

/**
 * A wrapper for the <a href="http://openjdk.java.net/projects/nashorn/">Nashorn JavaScript engine</a>.
 *
 * @author Ahmed Ashour
 */
public class NashornJavaScriptEngine implements AbstractJavaScriptEngine {

    private static final Log LOG = LogFactory.getLog(NashornJavaScriptEngine.class);

    /**
     * Classes.
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends ScriptObject>[] CLASSES_ = new Class[] {
//            Attr2.class,
//            CSSStyleDeclaration2.class,
//            CharacterData2.class,
//            Comment2.class,
//            ComputedCSSStyleDeclaration2.class,
//            Document2.class,
//            Element2.class,
//            Event2.class,
//            EventTarget2.class,
//            History2.class,
//            HTMLAllCollection2.class,
//            HTMLBodyElement2.class,
//            HTMLCollection2.class,
//            HTMLDivElement2.class,
//            HTMLDocument2.class,
//            HTMLElement2.class,
//            HTMLEmbedElement2.class,
//            HTMLFormElement2.class,
//            HTMLFrameElement2.class,
//            HTMLHtmlElement2.class,
//            HTMLIFrameElement2.class,
//            HTMLImageElement2.class,
//            HTMLInputElement2.class,
//            HTMLMapElement2.class,
//            HTMLObjectElement2.class,
//            HTMLSelectElement2.class,
//            HTMLSpanElement2.class,
//            HTMLTextAreaElement2.class,
//            HTMLTitleElement2.class,
//            HTMLUnknownElement2.class,
//            Location2.class,
//            MessageEvent2.class,
//            MouseEvent2.class,
//            Node2.class,
//            NodeList2.class,
//            Selection2.class,
//            StyleSheetList2.class,
//            Text2.class,
//            UIEvent2.class,
//            Window2.class,
//            XMLDocument2.class,
//            XMLHttpRequest2.class,
//            XPathResult2.class
        };

    private transient ThreadLocal<Boolean> javaScriptRunning_;
    private transient ThreadLocal<List<PostponedAction>> postponedActions_;
    private transient boolean holdPostponedActions_;

    private final WebClient webClient_;
    /** The JavaScriptExecutor corresponding to all windows of this Web client. */
    private transient JavaScriptExecutor javaScriptExecutor_;

    private final NashornScriptEngine engine_
        = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();

    /**
     * Creates an instance for the specified {@link WebClient}.
     *
     * @param webClient the client that will own this engine
     */
    public NashornJavaScriptEngine(final WebClient webClient) {
        webClient_ = webClient;
        initTransientFields();
    }

    private void initTransientFields() {
        javaScriptRunning_ = new ThreadLocal<>();
        postponedActions_ = new ThreadLocal<>();
        holdPostponedActions_ = false;
    }

    private static Browser getBrowser(final BrowserVersion version) {
        final BrowserFamily family;
        if (version.isFirefox()) {
            family = BrowserFamily.FF;
        }
        else if (version.isIE()) {
            family = BrowserFamily.IE;
        }
        else if (version.isEdge()) {
            family = BrowserFamily.EDGE;
        }
        else {
            family = BrowserFamily.CHROME;
        }
        return new Browser(family, (int) version.getBrowserVersionNumeric());
    }

    private void initGlobal(final WebWindow webWindow, final Browser browser) {
        Browser.setCurrent(browser);
        final Global global = getGlobal(webWindow);
        final Global oldGlobal = Context.getGlobal();
        try {
            Context.setGlobal(global);

            try {
                final Map<String, String> javaSuperMap = new HashMap<>();
                final Map<String, String> javaJavaScriptMap = new HashMap<>();
                for (final Class<?> klass : CLASSES_) {
                    for (final Class<?> inner : klass.getDeclaredClasses()) {
                        final ClassConstructor constructor = inner.getAnnotation(ClassConstructor.class);
                        if (isSupported(constructor, browser)) {
                            final ScriptObject instance = (ScriptObject) inner.newInstance();
                            String className = instance.getClassName();
                            if (instance instanceof ScriptFunction) {
                                className = ((ScriptFunction) instance).getName();
                            }
                            global.put(className, instance, false);
                            if (!isNullProto(klass)) {
                                javaSuperMap.put(klass.getName(), getSuperScriptClassName(klass));
                            }
                            javaJavaScriptMap.put(klass.getName(), className);
                        }
                    }
                }
                final Object eventTarget = global.get("EventTarget");
                if (eventTarget instanceof ScriptFunction) {
                    final ScriptFunction parentFunction = (ScriptFunction) eventTarget;
                    final PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
                    global.setProto(parentPrototype);
                }
                else {
//                    global.put("Window", new Window2.ObjectConstructor(), true);
//                    global.setProto(new EventTarget2.ObjectConstructor());
                }

                for (final String javaClassName : javaSuperMap.keySet()) {
                    final String javaSuperClassName = javaSuperMap.get(javaClassName);
                    final String superJavaScriptName = javaJavaScriptMap.get(javaSuperClassName);
                    if (superJavaScriptName != null) {
                        setProto(global, javaJavaScriptMap.get(javaClassName), superJavaScriptName);
                    }
                }

                final String[] toBeRemoved = {"java", "javax", "javafx", "org", "com", "net", "edu", "JavaAdapter",
                    "JSAdapter", "JavaImporter", "Packages", "arguments", "load", "loadWithNewGlobal", "exit", "quit",
                    "Java", "__noSuchProperty__", "javax.script.filename"};
                for (final String key : toBeRemoved) {
                    global.remove(key, true);
                }

//                final Window2 window = new Window2();
//                ScriptObject windowProto = Context.getGlobal().getPrototype(window.getClass());
//                if (windowProto == null) {
//                    windowProto = (ScriptObject) global.get("Window");
//                }
//                window.setProto(windowProto);
//                ScriptUtils.initialize(window);
//
//                global.put("window", global, true);
//                global.setWindow(window);
//
//                for (final Property property : windowProto.getMap().getProperties()) {
//                    final Object value = property.getObjectValue(windowProto, windowProto);
//                    global.put(property.getKey(), value, true);
//                }
//
//                final List<Property> windowProperties = Arrays.asList(window.getMap().getProperties());
//                global.setMap(global.getMap().addAll(PropertyMap.newMap(windowProperties)));
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        finally {
            Context.setGlobal(oldGlobal);
        }
    }

    private static String getSuperScriptClassName(final Class<?> klass) {
        Class<?> superClass;
        for (superClass = klass.getSuperclass();
                superClass.getAnnotation(ScriptClass.class) == null; superClass = superClass.getSuperclass()) {
            if (superClass == ScriptObject.class) {
                return "";
            }
        }
        return superClass.getName();
    }

    private static boolean isSupported(final ClassConstructor constructor, final Browser browser) {
        final int version = browser.getVersion();
        if (constructor != null) {
            for (final WebBrowser webBrowser : constructor.value()) {
                if (webBrowser.value() == browser.getFamily()
                        && webBrowser.minVersion() <= version
                        && webBrowser.maxVersion() >= version) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isNullProto(final Class<?> enclosingClass) {
        final ScriptClass scriptClass = enclosingClass.getAnnotation(ScriptClass.class);
        return scriptClass == null || scriptClass.nullProto();
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object o, final String fieldName) {
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(o);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setProto(final Global global, final String childName, final String parentName) {
        final ScriptObject child = (ScriptObject) global.get(childName);
        final ScriptObject parent = (ScriptObject) global.get(parentName);
        if (child instanceof ScriptFunction && parent instanceof ScriptFunction) {
            final ScriptFunction childFunction = (ScriptFunction) child;
            final PrototypeObject childPrototype = (PrototypeObject) childFunction.getPrototype();
            final ScriptFunction parentFunction = (ScriptFunction) parent;
            final PrototypeObject parentPrototype = (PrototypeObject) parentFunction.getPrototype();
            childPrototype.setProto(parentPrototype);
            childFunction.setProto(parentFunction);
        }
        else {
            child.setProto(parent);
        }
    }

    @Override
    public JavaScriptConfiguration getJavaScriptConfiguration() {
        return null;
    }

    @Override
    public void addPostponedAction(final PostponedAction action) {
        List<PostponedAction> actions = postponedActions_.get();
        if (actions == null) {
            actions = new ArrayList<>();
            postponedActions_.set(actions);
        }
        actions.add(action);
    }

    @Override
    public void processPostponedActions() {
        doProcessPostponedActions();
    }

    private void doProcessPostponedActions() {
        holdPostponedActions_ = false;

        try {
            getWebClient().loadDownloadedResponses();
        }
        catch (final RuntimeException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final List<PostponedAction> actions = postponedActions_.get();
        if (actions != null) {
            postponedActions_.set(null);
            try {
                for (final PostponedAction action : actions) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Processing PostponedAction " + action);
                    }

                    // verify that the page that registered this PostponedAction is still alive
                    if (action.isStillAlive()) {
                        action.execute();
                    }
                }
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

//    public Object execute(final HtmlPage page, final ScriptFunction script) {
//        try {
//            return engine.evalImpl(script, page.getEnclosingWindow().getScriptContext());
//        }
//        catch (final Exception e) {
//            handleJavaScriptException(new ScriptException(page, e, script.safeToString()), true);
//            return null;
//        }
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(final HtmlPage page, final String sourceCode, final String sourceName,
            final int startLine) {
        final Boolean javaScriptAlreadyRunning = javaScriptRunning_.get();
        javaScriptRunning_.set(Boolean.TRUE);
//        Object response;
        synchronized (page) { // 2 scripts can't be executed in parallel for one page
            if (page != page.getEnclosingWindow().getEnclosedPage()) {
                return null; // page has been unloaded
            }
//            try {
//                response = engine.eval(sourceCode, page.getEnclosingWindow().getScriptContext());
//            }
//            catch (final Exception e) {
//                handleJavaScriptException(new ScriptException(page, e, sourceCode), true);
//                return null;
//            }
        }

        // doProcessPostponedActions is synchronized
        // moved out of the sync block to avoid deadlocks
        if (!holdPostponedActions_) {
            doProcessPostponedActions();
        }
        return null;
//        return response;
    }

    /**
     * Compiles the specified JavaScript code in the context of a given HTML page.
     *
     * @param page the page that the code will execute within
     * @param sourceCode the JavaScript code to execute
     * @param sourceName the name that will be displayed on error conditions
     * @param startLine the line at which the script source starts
     * @return the result of executing the specified code
     */
    public ScriptFunction compile(final HtmlPage page, final String sourceCode,
            final String sourceName, final int startLine) {
//        final Global global = page.getEnclosingWindow().getGlobal();
//        final Global oldGlobal = Context.getGlobal();
//        try {
//            Context.setGlobal(global);
//
//            return global.getContext().compileScript(Source.sourceFor("<eval>", sourceCode), global);
//        }
//        finally {
//            Context.setGlobal(oldGlobal);
//        }
        return null;
    }

    /**
     * Returns the web client that this engine is associated with.
     * @return the web client
     */
    public WebClient getWebClient() {
        return webClient_;
    }

    /**
     * Handles an exception that occurred during execution of JavaScript code.
     * @param scriptException the exception
     * @param triggerOnError if true, this triggers the onerror handler
     */
    protected void handleJavaScriptException(final ScriptException scriptException, final boolean triggerOnError) {
        // Trigger window.onerror, if it has been set.
//        final HtmlPage page = scriptException.getPage();
//        if (triggerOnError && page != null) {
//            final WebWindow webWindow = page.getEnclosingWindow();
//            if (webWindow != null) {
//                final Window2 window = webWindow.getGlobal().getWindow();
//                if (window != null) {
//                    try {
//                        window.triggerOnError(scriptException);
//                    }
//                    catch (final Exception e) {
//                        handleJavaScriptException(new ScriptException(page, e, null), false);
//                    }
//                }
//            }
//        }
//        final JavaScriptErrorListener javaScriptErrorListener = getWebClient().getJavaScriptErrorListener();
//        if (javaScriptErrorListener != null) {
//            javaScriptErrorListener.scriptException(page, scriptException);
//        }
//        // Throw a Java exception if the user wants us to.
//        if (getWebClient().getOptions().isThrowExceptionOnScriptError()) {
//            throw scriptException;
//        }
//        // Log the error; ScriptException instances provide good debug info.
//        LOG.info("Caught script exception", scriptException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerWindowAndMaybeStartEventLoop(final WebWindow webWindow) {
        if (javaScriptExecutor_ == null) {
            javaScriptExecutor_ = BackgroundJavaScriptFactory.theFactory().createJavaScriptExecutor(webClient_);
        }
        javaScriptExecutor_.addWindow(webWindow);
    }

    /**
     * Returns theg {@link Global}.
     * @param webWindow the webwindow
     * @return the {@link Global}
     */
    public static Global getGlobal(final WebWindow webWindow) {
        return null;
//        final ScriptContext context = webWindow.getScriptContext();
//        return get(context.getBindings(ScriptContext.ENGINE_SCOPE), "sobj");
    }

    @Override
    public void initialize(final WebWindow webWindow) {
//        final Global global = engine.createNashornGlobal();
//        final ScriptContext scriptContext = webWindow.getScriptContext();
//        scriptContext.setBindings(new ScriptObjectMirror(global, global), ScriptContext.ENGINE_SCOPE);
//        initGlobal(webWindow, getBrowser(webClient_.getBrowserVersion()));
//        global.<Window2>getWindow().setWebWindow(webWindow);
//        webWindow.setScriptObject(global);
    }

    @Override
    public void setJavaScriptTimeout(final long timeout) {
    }

    @Override
    public long getJavaScriptTimeout() {
        return 0;
    }

    @Override
    public void shutdown() {
        if (javaScriptExecutor_ != null) {
            javaScriptExecutor_.shutdown();
            javaScriptExecutor_ = null;
        }
        if (postponedActions_ != null) {
            postponedActions_.remove();
        }
        if (javaScriptRunning_ != null) {
            javaScriptRunning_.remove();
        }
        holdPostponedActions_ = false;
    }

    @Override
    public boolean isScriptRunning() {
        return false;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br>
     * Indicates that no postponed action should be executed.
     */
    public void holdPosponedActions() {
        holdPostponedActions_ = true;
    }

}
