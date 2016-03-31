/*
 * Copyright (c) 2002-2016 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.javascript.host;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;

/**
 * Tests for {@link WebSocket}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Madis Pärn
 */
@RunWith(BrowserRunner.class)
public class WebSocketTest extends WebDriverTestCase {

    /**
     * Test case taken from <a href="http://angelozerr.wordpress.com/2011/07/23/websockets_jetty_step1/">here</a>.
     * @throws Exception if the test fails
     */
    @Test
    public void chat() throws Exception {
        final String firstResponse = "Browser: has joined!";
        final String secondResponse = "Browser: Hope you are fine!";

        startWebServer("src/test/resources/com/gargoylesoftware/htmlunit/javascript/host",
            null, null, new ChatWebSocketHandler());
        final WebDriver driver = getWebDriver();
        driver.get("http://localhost:" + PORT + "/WebSocketTest_chat.html");

        driver.findElement(By.id("username")).sendKeys("Browser");
        driver.findElement(By.id("joinB")).click();

        assertVisible("joined", driver);

        final WebElement chatE = driver.findElement(By.id("chat"));
        int counter = 0;
        do {
            Thread.sleep(100);
        } while (chatE.getText().isEmpty() && counter++ < 10);

        assertEquals(firstResponse, chatE.getText());

        driver.findElement(By.id("phrase")).sendKeys("Hope you are fine!");
        driver.findElement(By.id("sendB")).click();
        counter = 0;
        do {
            Thread.sleep(100);
        } while (!chatE.getText().contains(secondResponse) && counter++ < 10);

        assertEquals(firstResponse + "\n" + secondResponse, chatE.getText());
    }

    private static class ChatWebSocketHandler extends WebSocketHandler {

        private final Set<ChatWebSocket> webSockets_ = new CopyOnWriteArraySet<>();

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(ChatWebSocket.class);
            factory.setCreator(new WebSocketCreator() {
                @Override
                public Object createWebSocket(final ServletUpgradeRequest servletUpgradeRequest,
                        final ServletUpgradeResponse servletUpgradeResponse) {
                    return new ChatWebSocket();
                }
            });
        }

        private class ChatWebSocket extends WebSocketAdapter {
            private Session session_;

            @Override
            public void onWebSocketConnect(final Session session) {
                this.session_ = session;
                webSockets_.add(this);
            }

            @Override
            public void onWebSocketText(final String data) {
                try {
                    for (final ChatWebSocket webSocket : webSockets_) {
                        webSocket.session_.getRemote().sendString(data);
                    }
                }
                catch (final IOException x) {
                    session_.close();
                }
            }

            @Override
            public void onWebSocketClose(final int closeCode, final String message) {
                webSockets_.remove(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @After
    @Override
    public void releaseResources() {
        super.releaseResources();
        for (final Thread thread : Thread.getAllStackTraces().keySet()) {
            assertFalse("WebSocket threads still running", thread.getName().contains("WebSocket"));
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ ": myname=My value!1", ": myname=My value!2" })
    public void cookies() throws Exception {
        startWebServer("src/test/resources/com/gargoylesoftware/htmlunit/javascript/host",
            null, null, new CookiesWebSocketHandler());
        final WebDriver driver = getWebDriver();
        driver.get("http://localhost:" + PORT + "/WebSocketTest_cookies.html");

        driver.findElement(By.id("username")).sendKeys("Browser");
        driver.findElement(By.id("joinB")).click();
        final WebElement chatE = driver.findElement(By.id("chat"));
        int counter = 0;
        do {
            Thread.sleep(100);
        } while (chatE.getText().isEmpty() && counter++ < 10);

        final String[] expected = getExpectedAlerts();
        assertEquals(expected[0], chatE.getText());

        driver.findElement(By.id("phrase")).sendKeys("Hope you are fine!");
        driver.findElement(By.id("sendB")).click();
        counter = 0;
        do {
            Thread.sleep(100);
        } while (!chatE.getText().contains(expected[1]) && counter++ < 10);

        assertEquals(expected[0] + "\n" + expected[1], chatE.getText());
    }

    private static class CookiesWebSocketHandler extends WebSocketHandler {

        private final Set<CookiesWebSocket> webSockets_ = new CopyOnWriteArraySet<>();

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(CookiesWebSocket.class);
            factory.setCreator(new WebSocketCreator() {
                @Override
                public Object createWebSocket(final ServletUpgradeRequest servletUpgradeRequest,
                        final ServletUpgradeResponse servletUpgradeResponse) {
                    return new CookiesWebSocket();
                }
            });
        }

        private class CookiesWebSocket extends WebSocketAdapter {
            private Session session_;
            private int counter_ = 1;

            @Override
            public void onWebSocketConnect(final Session session) {
                this.session_ = session;
                webSockets_.add(this);
            }

            @Override
            public void onWebSocketText(final String data) {
                try {
                    final String cookie = session_.getUpgradeRequest().getHeaders().get("Cookie").get(0) + counter_++;
                    for (final CookiesWebSocket webSocket : webSockets_) {
                        webSocket.session_.getRemote().sendString(cookie);
                    }
                }
                catch (final IOException x) {
                    session_.close();
                }
            }

            @Override
            public void onWebSocketClose(final int closeCode, final String message) {
                webSockets_.remove(this);
            }
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented
    public void events() throws Exception {
        startWebServer("src/test/resources/com/gargoylesoftware/htmlunit/javascript/host",
            null, null, new EventsWebSocketHandler());
        final WebDriver driver = getWebDriver();
        driver.get("http://localhost:" + PORT + "/WebSocketTest_events.html");

        assertVisible("onOpen", driver);
        assertVisible("onMessageText", driver);
        assertVisible("onMessageBinary", driver);
        assertVisible("onClose", driver);
    }

    private void assertVisible(final String domId, final WebDriver driver) throws Exception {
        final WebElement domE = driver.findElement(By.id(domId));
        int counter = 0;
        do {
            Thread.sleep(100);
        } while (!domE.isDisplayed() && counter++ < 10);

        assertEquals("Node should be visible, domId: " + domId, true, domE.isDisplayed());
    }

    private static class EventsWebSocketHandler extends WebSocketHandler {

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(EventsWebSocket.class);
            factory.setCreator(new WebSocketCreator() {
                @Override
                public EventsWebSocket createWebSocket(final ServletUpgradeRequest servletUpgradeRequest,
                        final ServletUpgradeResponse servletUpgradeResponse) {
                    return new EventsWebSocket();
                }
            });
        }

        private static class EventsWebSocket extends WebSocketAdapter {
            @Override
            public void onWebSocketText(final String data) {
                if ("text".equals(data)) {
                    try {
                        getRemote().sendString("server_text");
                    }
                    catch (final IOException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
                else if ("close".equals(data)) {
                    getSession().close();
                }
                else {
                    throw new IllegalArgumentException("Unknown request: " + data);
                }
            }

            @Override
            public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
                final String data = new String(payload, offset, len, UTF_16LE);
                if ("binary".equals(data)) {
                    final ByteBuffer response = ByteBuffer.wrap("server_binary".getBytes(UTF_16LE));
                    try {
                        getRemote().sendBytes(response);
                    }
                    catch (final IOException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
                else {
                    throw new IllegalArgumentException("Unknown request: " + data);
                }
            }
        }
    }
}
