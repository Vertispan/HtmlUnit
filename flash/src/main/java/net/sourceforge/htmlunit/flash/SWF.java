package net.sourceforge.htmlunit.flash;

import java.io.IOException;
import java.io.InputStream;

import adobe.abc.GlobalOptimizer;
import adobe.abc.GlobalOptimizer.InputAbc;
import flash.swf.TagDecoder;
import flash.swf.TagHandler;
import flash.swf.tags.DoABC;

public class SWF {

    public SWF(final InputStream is) throws IOException {
        TagDecoder decoder = new TagDecoder(is);
        decoder.parse(new SWFTagHandler());
    }

    private static class SWFTagHandler extends TagHandler {
        public void doABC(final DoABC tag) {
            try {
                final GlobalOptimizer go = new GlobalOptimizer();
                
                final InputAbc ia = go.new InputAbc();
                ia.readAbc(tag.abc);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
