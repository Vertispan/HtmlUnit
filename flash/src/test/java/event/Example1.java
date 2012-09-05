package event;

import java.io.InputStream;

import org.junit.Test;

public class Example1 {

    @Test
    public void test() throws Exception {
        final InputStream in = getClass().getResourceAsStream("ev_example1.swf");
        try {
            //SwfDecoder decoder = new SwfDecoder(in, 10);
        }
        finally {
            in.close();
        }
    }

}
