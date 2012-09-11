/*
 * Copyright (c) 2002-2012 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.htmlunit.flash;

import macromedia.asc.embedding.avmplus.ActionBlockConstants;
import adobe.abc.Binding;
import adobe.abc.Block;
import adobe.abc.Expr;
import adobe.abc.GlobalOptimizer.InputAbc;
import adobe.abc.Method;
import adobe.abc.Type;
import event.Example1;

/**
 * The engine.
 *
 * @version $Revision$
 * @author Ahmed Ashour
 */
public final class ActionScriptEngine {

    private static final boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        new Example1().test();
    }

    private ActionScriptEngine() {
    }

    public static void execute(final InputAbc ia) {
        for (Type t : ia.classes) {
            final String thisClass = t.itype.getName().toString();
            final String superClassName = t.itype.base.getName().toString();
            RuntimeScriptObject runtimeObject = new RuntimeScriptObject(thisClass, superClassName);
            ActionScriptConfiguration.setPrototype(thisClass, runtimeObject);
            for (final Binding b : t.itype.defs.values()) {
                runtimeObject.putProperty(b.getName().toString(), b.method);
            }
        }
        final Type initType = ia.scripts[ia.scripts.length - 1].ref.getType();
        for (final Method method : ia.methods) {
            if (method.getName().equals(initType.getName())) {
                execute(method, null);
            }
        }
    }

    public static void execute(final Method method, final ScriptObject thisObj) {
        execute(method.entry.to, thisObj);
    }

    public static void execute(final Block block, final ScriptObject thisObj) {
        for (final Expr e : block.exprs) {
            execute(e, thisObj);
        }
    }

    public static Object execute(final Expr e, final ScriptObject thisObj) {
        switch (e.op) {
        case 0:
            if (DEBUG) {
                System.out.println(e.ref);
            }
            if (e.ref.format().equals("public::this")) {
                return thisObj;
            }
            break;

        case ActionBlockConstants.OP_pushscope:
            Debugger.ensure(e.args.length == 1);
            if (DEBUG) {
                System.out.println(Util.getOpcodeName(e.op) + ' ' + e.args[0].ref);
            }
            break;

        case ActionBlockConstants.OP_getproperty:
            Debugger.ensure(e.args.length == 1);
            if (DEBUG) {
                System.out.println(Util.getOpcodeName(e.op) + ' '+ e.args[0].ref + ' ' + '.' + e.ref);
            }
            ScriptObject object = (ScriptObject) execute(e.args[0], thisObj);
            if (object != null) {//to be removed
                return object.getProperty(e.ref.toString());
            }
            break;

        case ActionBlockConstants.OP_callpropvoid:
            if (e.args.length == 2) {
                if (DEBUG) {
                    System.out.println(Util.getOpcodeName(e.op) + ' ' + e.args[0].ref + "." +  e.ref + '(' + e.args[1].ref + ')');
                }
            }
            else if (e.args.length == 3) {
                Object function = execute(e.args[0], thisObj);
                Object[] args = new Object[e.args.length - 1];
                for (int i = 0; i < args.length; i++) {
                    args[i] = execute(e.args[i + 1], thisObj);
                }
                if (function instanceof java.lang.reflect.Method) {
                    try {
                        NativeScriptObject nativeObject;
                        if (thisObj instanceof NativeScriptObject) {
                            nativeObject = (NativeScriptObject) thisObj;
                        }
                        else {
                            nativeObject = ((RuntimeScriptObject) thisObj).getNativeObject();
                        }
                        ((java.lang.reflect.Method) function).invoke(nativeObject.getObject(), args);
                    }
                    catch(final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else {
                throw new AssertionError();
            }
            break;

        case ActionBlockConstants.OP_findpropstrict:
            Debugger.ensure(e.args.length == 0);
            final String property = e.ref.toString();
            for (Expr scope : e.scopes) {
                if (scope.op == ActionBlockConstants.OP_pushscope) {
                    if (scope.args[0].op == 0 && scope.args[0].ref.toString().equals("this")) {
                        if (thisObj != null) {
                            return thisObj.getProperty(property);
                        }
                    }
                    else {
                        if (DEBUG) {
                            System.out.println("OP_findpropstrict " + scope.args[0].ref);
                        }
                    }
                }
                else {
                    throw new AssertionError();
                }
            }
            break;

        case ActionBlockConstants.OP_jump:
            Debugger.ensure(e.succ.length == 1);
            if (DEBUG) {
                System.out.println("JUMP");
            }
            execute(e.succ[0].to, thisObj);
            break;

        case ActionBlockConstants.OP_newclass:
            Debugger.ensure(e.args.length == 1);
            execute(e.c.init, null);
            final String thisClass = e.c.itype.getName().toString();
            ScriptObject prototype = ActionScriptConfiguration.getPrototypeOf(thisClass);
            final ScriptObject scriptObject;
            if (prototype instanceof RuntimeScriptObject) {
                scriptObject = new RuntimeScriptObject((RuntimeScriptObject) prototype);
            }
            else {
                scriptObject = new NativeScriptObject((NativeScriptObject) prototype);
            }
            execute(e.c.itype.init, scriptObject);
            break;

        case ActionBlockConstants.OP_constructsuper:
            Debugger.ensure(e.args.length == 1);
            if (DEBUG) {
                System.out.println(Util.getOpcodeName(e.op) + ' '+ e.args[0].ref + ' ' + '.' + e.ref);
            }
            break;

        case ActionBlockConstants.OP_pushbyte:
            return e.value;

        case ActionBlockConstants.OP_popscope:
        case ActionBlockConstants.OP_returnvoid:
            //nothing for now
            break;
        default:
            if (DEBUG) {
                System.out.println(Util.getOpcodeName(e.op) + " " + e.args.length);
            }
        }
        return null;
    }
}
