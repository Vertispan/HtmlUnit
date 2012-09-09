package net.sourceforge.htmlunit.flash;

import java.lang.reflect.Field;

import macromedia.asc.embedding.avmplus.ActionBlockConstants;

public class Util {

    public static String getOpcodeName(final int op) {
        try {
            for (final Field f : ActionBlockConstants.class.getFields()) {
                if (f.getName().startsWith("OP_") && (Integer) f.get(null) == op) {
                    return f.getName().substring(3);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (op == 0) {
            return "XXX";
        }
        return null;
    }
}
