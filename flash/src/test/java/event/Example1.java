package event;

import java.io.InputStream;

import net.sourceforge.htmlunit.flash.Flash;

import org.junit.Test;

public class Example1 {

    @Test
    public void test() throws Exception {
        final InputStream in = getClass().getResourceAsStream("as3keyboard.swf");
        new Flash(in);
    }

}
