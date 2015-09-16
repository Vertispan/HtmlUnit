package jdk.nashorn;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import jdk.internal.dynalink.CallSiteDescriptor;
import jdk.internal.dynalink.linker.GuardedInvocation;
import jdk.internal.dynalink.linker.LinkRequest;
import jdk.nashorn.internal.runtime.ScriptObject;

public class SomeHost extends ScriptObject {

    public String aMethod() {
        return "in aMethod()";
    }

    @Override
    protected GuardedInvocation findGetMethod(CallSiteDescriptor desc, LinkRequest request, String operator) {
        return new GuardedInvocation(virtualHandle("aMethod", String.class), (MethodHandle) null);
    }

    private static MethodHandle virtualHandle(String name, Class<?> rtype, Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findVirtual(SomeHost.class, name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }

}
