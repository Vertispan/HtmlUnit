package jdk2;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import jdk2.nashorn.internal.objects.NativeError;
import jdk2.nashorn.internal.objects.PrototypeObject;
import jdk2.nashorn.internal.objects.ScriptFunctionImpl;
import jdk2.nashorn.internal.objects.annotations.Function;
import jdk2.nashorn.internal.objects.annotations.Getter;
import jdk2.nashorn.internal.objects.annotations.ScriptClass;
import jdk2.nashorn.internal.runtime.AccessorProperty;
import jdk2.nashorn.internal.runtime.Context;
import jdk2.nashorn.internal.runtime.Property;
import jdk2.nashorn.internal.runtime.PropertyMap;
import jdk2.nashorn.internal.runtime.ScriptFunction;
import jdk2.nashorn.internal.runtime.ScriptObject;

@ScriptClass("Abc3")
public class Abc3 extends ScriptObject {

    private static PropertyMap $nasgenmap$;

    public Abc3() {
        super($nasgenmap$);
    }

    @jdk2.nashorn.internal.objects.annotations.Constructor
    public static Abc3 myConstructor(final boolean newObj, final Object self) {
        return new Abc3();
    }

    @Function
    public static void hello3(Object hi) {
        Context context = Context.getContext();
        System.out.println(context.getEnv().getArguments());
        System.out.println("Hello3");
    }

    @Getter
    public static String getY(Object hi) {
        System.out.println("Hi3");
        return "hello3";
    }


    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return "wak wak 3";
    }


    static {
            final List<Property> list = new ArrayList<>(1);
            list.add(AccessorProperty.create("getY", 0, 
                    staticHandle("getY", String.class, Object.class),
null));
            $nasgenmap$ = PropertyMap.newMap(list);
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(Abc3.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptFunctionImpl {
        private static final PropertyMap $nasgenmap$;

        static {
            final List<Property> list = new ArrayList<>(1);
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        public Constructor() {
            super("Abc3", 
                    staticHandle("myConstructor", Abc3.class, boolean.class, Object.class),
                    $nasgenmap$, null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                Constructor.class.getDeclaredMethod("name", ptypes);
                return MethodHandles.lookup().findVirtual(Constructor.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    static final class Prototype extends PrototypeObject {
        private ScriptFunction hello3;
        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$hello3() {
            return this.hello3;
        }

        public void S$hello3(final ScriptFunction function) {
            this.hello3 = function;
        }

        static {
            final List<Property> list = new ArrayList<>(2);
            list.add(AccessorProperty.create("hello3", 0, 
                    virtualHandle("G$hello3", ScriptFunction.class),
                    virtualHandle("S$hello3", void.class, ScriptFunction.class)));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
            super($nasgenmap$);
            hello3 = ScriptFunctionImpl.makeFunction("hello3",
                    staticHandle("hello3", void.class, Object.class));
        }

       public String getClassName() {
           return "Abc3";
       }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Prototype.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
