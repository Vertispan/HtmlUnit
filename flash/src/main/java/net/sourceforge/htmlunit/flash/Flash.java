package net.sourceforge.htmlunit.flash;

import java.io.IOException;
import java.io.InputStream;

import macromedia.asc.embedding.avmplus.ActionBlockConstants;
import adobe.abc.Block;
import adobe.abc.Expr;
import adobe.abc.GlobalOptimizer;
import adobe.abc.GlobalOptimizer.InputAbc;
import adobe.abc.Method;
import event.Example1;
import flash.swf.Tag;
import flash.swf.TagDecoder;
import flash.swf.TagHandler;
import flash.swf.tags.DoABC;

public class Flash {

    public static void main(String[] args) throws Exception {
        new Example1().test();
    }

    public Flash(final InputStream is) throws IOException {
        TagDecoder decoder = new TagDecoder(is);
        decoder.parse(new FlashTagHandler());
    }

    private static class FlashTagHandler extends TagHandler {
        public void any(Tag tag) {
            //System.out.println(tag.getClass().getName());
        }
        public void doABC(final DoABC tag) {
            try {
                final GlobalOptimizer go = new GlobalOptimizer();
                
                final InputAbc ia = go.new InputAbc();
                ia.readAbc(tag.abc);

                for (final Method m : ia.methods) {
                    System.out.println("-------------------------------------");
                    System.out.println(m.getName());
                    debug(m.entry.to);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static void debug(Block b) {
        for (Expr e : b.exprs) {
            switch (e.op) {
            case 0:
                System.out.println(e.ref);
                break;

            case ActionBlockConstants.OP_pushscope:
                ensure(e.args.length == 1);
                System.out.println(Util.getOpcodeName(e.op) + ' ' + e.args[0].ref);
                break;

            case ActionBlockConstants.OP_getproperty:
                ensure(e.args.length == 1);
                System.out.println(Util.getOpcodeName(e.op) + ' '+ e.args[0].ref + ' ' + '.' + e.ref);
                break;

            case ActionBlockConstants.OP_callpropvoid:
                if (e.args.length == 2) {
                    System.out.println(Util.getOpcodeName(e.op) + ' ' + e.args[0].ref + "." +  e.ref + '(' + e.args[1].ref + ')');
                }
                else if (e.args.length == 3) {
                    System.out.println(Util.getOpcodeName(e.op) + ' ' + e.args[0].ref + "." + e.ref + '(' + e.args[1].ref + ", " + e.args[2].ref + ')');
                }
                else {
                    throw new AssertionError();
                }
                break;

            case ActionBlockConstants.OP_findpropstrict:
                ensure(e.args.length == 0);
                System.out.println(Util.getOpcodeName(e.op) + ' ' + e.ref.format());
                break;

            case ActionBlockConstants.OP_jump:
                ensure(e.succ.length == 1);
                System.out.println("JUMP");
                debug(e.succ[0].to);
                break;

            default:
                System.out.println(Util.getOpcodeName(e.op) + " " + e.args.length);
            }
        }
    }

    public static void ensure(final boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }
}
