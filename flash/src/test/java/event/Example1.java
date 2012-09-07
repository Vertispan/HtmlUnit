package event;

import java.io.InputStream;

import net.sourceforge.htmlunit.flash.SWF;

import org.junit.Test;

import flash.swf.Dictionary;

public class Example1 {
    Dictionary dict_;

    @Test
    public void test() throws Exception {
        final InputStream in = getClass().getResourceAsStream("ev_example1.swf");
        new SWF(in);
    }

}
