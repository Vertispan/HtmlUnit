package jdk2;

import static jdk2.nashorn.internal.objects.annotations.BrowserFamily.CHROME;
import static jdk2.nashorn.internal.objects.annotations.BrowserFamily.FF;
import static jdk2.nashorn.internal.objects.annotations.BrowserFamily.IE;

import jdk2.nashorn.internal.objects.annotations.Function;
import jdk2.nashorn.internal.objects.annotations.Getter;
import jdk2.nashorn.internal.objects.annotations.ScriptClass;
import jdk2.nashorn.internal.objects.annotations.Setter;
import jdk2.nashorn.internal.objects.annotations.WebBrowser;
import jdk2.nashorn.internal.runtime.Context;
import jdk2.nashorn.internal.runtime.PropertyMap;
import jdk2.nashorn.internal.runtime.ScriptObject;

@ScriptClass("Abc")
public class Abc extends ScriptObject {

    private static PropertyMap $nasgenmap$;

    public Abc() {
        super($nasgenmap$);
    }

    public Abc(PropertyMap map) {
        super(map.addAll($nasgenmap$));
    }

    //@jdk2.nashorn.internal.objects.annotations.Constructor
    public static Abc myConstructor(final boolean newObj, final Object self) {
        return new Abc();
    }

    @Function(browsers = @WebBrowser(FF))
    public static void hello(Object hi) {
        System.out.println("hello " + hi.getClass().getName());
        System.out.println("HOHO " + Context.getGlobal().get("HOHO"));
        System.out.println("Hello");
    }

    @Getter(browsers = { @WebBrowser(value = IE, minVersion = 1), @WebBrowser(CHROME) })
    public static String something(Object hi) {
        System.out.println("Hi");
        return "hello";
    }

    @Setter(browsers = { @WebBrowser(CHROME) })
    public static void something(Object hi, String abc) {
        System.out.println("Hi");
    }
}
