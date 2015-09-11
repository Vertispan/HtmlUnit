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
package com.gargoylesoftware.js;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * Test of coding style for things that cannot be detected by Checkstyle.
 *
 * @author Ahmed Ashour
 */
public class CodeStyleTest {

    private static final Pattern leadingWhitespace = Pattern.compile("^\\s+");

    /**
     * List of extensions for files which should have SVN eol-style property with {@code native} value.
     */
    private static List<String> EOL_EXTENSIONS_
        = Arrays.asList(".html", ".htm", ".js", ".css", ".xml", ".txt", ".properties", "*.php",
                "*.ini", "*.sh", "*.bat", "*.log", "*.java");

    private List<String> failures_ = new ArrayList<>();
    private SVNWCClient svnWCClient_;

    /**
     * After.
     */
    @After
    public void after() {
        if (svnWCClient_ != null) {
            svnWCClient_.getOperationsFactory().getRepositoryPool().dispose();
        }
        final StringBuilder sb = new StringBuilder();
        for (final String error : failures_) {
            sb.append('\n').append(error);
        }

        final int errorsNumber = failures_.size();
        if (errorsNumber == 1) {
            fail("CodeStyle error: " + sb);
        }
        else if (errorsNumber > 1) {
            fail("CodeStyle " + errorsNumber + " errors: " + sb);
        }
    }

    private void addFailure(final String error) {
        failures_.add(error);
    }

    /**
     * @throws IOException if the test fails
     */
    @Test
    public void codeStyle() throws IOException {
        final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        final ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
        svnWCClient_ = new SVNWCClient(authManager, options);
        process(new File("src/main"));
        process(new File("src/test"));
        licenseYear();
    }

    private void process(final File dir) throws IOException {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory() && !".svn".equals(file.getName())) {
                    process(file);
                }
                else {
                    final String relativePath = file.getAbsolutePath().substring(
                            new File(".").getAbsolutePath().length() - 1);
                    svnProperties(file, relativePath);
                }
            }
        }
    }

    /**
     * Checks property {@code svn:eol-style}.
     */
    private void svnProperties(final File file, final String relativePath) {
        if (!isSvnPropertiesDefined(file)) {
            addFailure("'svn:eol-style' property is not defined for " + relativePath);
        }
    }

    private boolean isSvnPropertiesDefined(final File file) {
        try {
            final AtomicBoolean eol = new AtomicBoolean();
            svnWCClient_.doGetProperty(file, null, SVNRevision.WORKING, SVNRevision.WORKING, SVNDepth.EMPTY,
                    new ISVNPropertyHandler() {

                    @Override
                    public void handleProperty(final long revision, final SVNPropertyData property) {
                        // nothing to do
                    }

                    @Override
                    public void handleProperty(final SVNURL url, final SVNPropertyData property) {
                        // nothing to do
                    }

                    @Override
                    public void handleProperty(final File path, final SVNPropertyData property) {
                        final String name = property.getName();
                        final String value = property.getValue().getString();
                        if ("svn:eol-style".equals(name) && "native".equals(value)) {
                            eol.set(true);
                        }
                    }
                }, null);

            final String fileName = file.getName().toLowerCase(Locale.ROOT);

            for (final String extension : EOL_EXTENSIONS_) {
                if (fileName.endsWith(extension)) {
                    return eol.get();
                }
            }
            return true;
        }
        catch (final Exception e) {
            //nothing
        }
        final String path = file.getAbsolutePath();
        // automatically generated and is outside SVN control
        return (path.contains("jQuery") && path.contains("WEB-INF") && path.contains("cgi"))
                || (path.contains("jQuery") && path.contains("csp.log"));
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void xmlStyle() throws Exception {
        processXML(new File("."), false);
        processXML(new File("src/main/resources"), true);
        processXML(new File("src/assembly"), true);
        processXML(new File("src/changes"), true);
    }

    private void processXML(final File dir, final boolean recursive) throws Exception {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory() && !".svn".equals(file.getName())) {
                    if (recursive) {
                        processXML(file, true);
                    }
                }
                else {
                    if (file.getName().endsWith(".xml")) {
                        final List<String> lines = FileUtils.readLines(file);
                        final String relativePath = file.getAbsolutePath().substring(
                                new File(".").getAbsolutePath().length() - 1);
                        mixedIndentation(lines, relativePath);
                        trailingWhitespace(lines, relativePath);
                        badIndentationLevels(lines, relativePath);
                    }
                }
            }
        }
    }

    /**
     * Verifies that no XML files have mixed indentation (tabs and spaces, mixed).
     */
    private void mixedIndentation(final List<String> lines, final String relativePath) {
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (line.indexOf('\t') != -1) {
                addFailure("Mixed indentation in " + relativePath + ", line: " + (i + 1));
            }
        }
    }

    /**
     * Verifies that no XML files have trailing whitespace.
     */
    private void trailingWhitespace(final List<String> lines, final String relativePath) {
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (!line.isEmpty()) {
                final char last = line.charAt(line.length() - 1);
                if (Character.isWhitespace(last)) {
                    addFailure("Trailing whitespace in " + relativePath + ", line: " + (i + 1));
                }
            }
        }
    }

    /**
     * Verifies that no XML files have bad indentation levels (each indentation level is 4 spaces).
     */
    private void badIndentationLevels(final List<String> lines, final String relativePath) {
        for (int i = 0; i < lines.size(); i++) {
            final int indentation = getIndentation(lines.get(i));
            if (indentation % 4 != 0) {
                addFailure("Bad indentation level (" + indentation + ") in " + relativePath + ", line: " + (i + 1));
            }
        }
    }

    /**
     * Checks the year in LICENSE.txt.
     */
    private void licenseYear() throws IOException {
        final List<String> lines = FileUtils.readLines(new File("LICENSE.txt"));
        if (!lines.get(1).contains("Copyright (c) " + Calendar.getInstance(Locale.ROOT).get(Calendar.YEAR))) {
            addFailure("Incorrect year in LICENSE.txt");
        }
    }

    private int getIndentation(final String line) {
        final Matcher matcher = leadingWhitespace.matcher(line);
        if (matcher.find()) {
            return matcher.end() - matcher.start();
        }
        return 0;
    }

}
