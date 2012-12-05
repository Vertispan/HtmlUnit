package net.sourceforge.htmlunit.htmlparser;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class NekoCanonicalTest {

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> collection = new ArrayList<Object[]>();
        File dir = new File("./src/test/resources");
        for (final File file : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html");
            }
        })) {
            collection.add(new Object[] { file });
        }
        return collection;
    }

    private File file_;
    public NekoCanonicalTest(final File file) {
        file_ = file;
    }
    
    @Test
    public void test() throws Exception {
        String html = IOUtils.toString(new FileReader(file_));
        StringReader stringReader = new StringReader(html);
        HTMLScanner reader = new HTMLScanner();
        CanonicalContentHandler contentHandler = new CanonicalContentHandler();
        reader.setContentHandler(contentHandler);
        reader.parse(new InputSource(stringReader));
        String actual = contentHandler.builder.toString().trim();
        String canonialName = file_.getName();
        canonialName = canonialName.substring(0, canonialName.lastIndexOf('.')) + ".canonical";
        String expected = IOUtils.toString(new FileReader(new File(file_.getParent(), canonialName)));
        expected = expected.replace("\r\n", "\n").trim();
        Assert.assertEquals(expected, actual);
    }

    private static class CanonicalContentHandler implements ContentHandler {

        private StringBuilder builder = new StringBuilder();
        
        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            builder.append('(').append(qName).append('\n');
            for (int i = 0; i < atts.getLength(); i++) {
                builder.append('A').append(atts.getQName(i)).append(' ').append(atts.getValue(i)).append('\n');
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            builder.append(')').append(qName).append('\n');
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            builder.append('"')
                .append(new String(ch, start, length).replaceAll(LINE_SEPARATOR, "\\\\n"))
                    .append('\n');
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length)
                throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data)
                throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }
        
    }
}