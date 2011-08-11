package net.sourceforge.htmlunit.htmlparser;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


public class HTMLScannerTest {

    @Test
    public void test() throws Exception {
        String html = "<hi hello=\"there\"></hi>";
        System.out.println("-------------- NekoHtml --------------");
        nekoHtml(html);
        System.out.println("------------- HtmlParser -------------");
        htmlParser(html);
        System.out.println("---------------- XML ----------------");
        xml(html);
    }

    private void nekoHtml(String html) throws Exception {
        StringReader stringReader = new StringReader(html);
        XMLParserConfiguration parser = new HTMLConfiguration();
        parser.setDocumentHandler(new SampleContentHandler());
        parser.parse(new XMLInputSource(null, null, null, stringReader, "UTF-8"));
    }

    private void htmlParser(String html) throws Exception {
        StringReader stringReader = new StringReader(html);
        HTMLScanner reader = new HTMLScanner();
        reader.setContentHandler(new SampleContentHandler());
        reader.parse(new InputSource(stringReader));
    }

    private void xml(String html) throws Exception {
        StringReader stringReader = new StringReader(html);
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        reader.setContentHandler(new SampleContentHandler());
        reader.parse(new InputSource(stringReader));
    }
}
