/*
 * Copyright (c) 2015 Gargoyle Software Inc.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (http://www.gnu.org/licenses/).
 */
package com.gargoylesoftware.js.nashorn.internal.tools.nasgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gargoylesoftware.js.nashorn.internal.objects.ArrayBufferView;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeArray;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeArrayBuffer;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeBoolean;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeDataView;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeDate;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeEvalError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeFloat32Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeFloat64Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeFunction;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeInt16Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeInt32Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeInt8Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeJSAdapter;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeJSON;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeJava;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeJavaImporter;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeMath;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeNumber;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeObject;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeRangeError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeReferenceError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeRegExp;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeString;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeSyntaxError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeTypeError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeURIError;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeUint16Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeUint32Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeUint8Array;
import com.gargoylesoftware.js.nashorn.internal.objects.NativeUint8ClampedArray;

public class JavaMain {

    public static void main(String[] args) throws Exception {
        final List<Class<?>> list = Arrays.asList(NativeFunction.class, NativeObject.class, NativeArray.class,
                NativeBoolean.class, NativeDate.class,
                NativeJSON.class, NativeJSAdapter.class,
                NativeMath.class, NativeNumber.class, NativeRegExp.class, 
                NativeString.class, NativeError.class, NativeEvalError.class,
                NativeRangeError.class, NativeReferenceError.class,
                NativeSyntaxError.class, NativeTypeError.class, NativeURIError.class,
                NativeJavaImporter.class, NativeJava.class,
                NativeArrayBuffer.class, NativeDataView.class,
                NativeInt8Array.class, NativeUint8Array.class, NativeUint8ClampedArray.class,
                NativeInt16Array.class, NativeUint16Array.class,
                NativeInt32Array.class, NativeUint32Array.class,
                NativeFloat32Array.class, NativeFloat64Array.class,
                ArrayBufferView.class);

        for(final Class<?> c : list) {
            process(c, false);
        }
        System.out.println("Finished");
    }

    public static void process(final Class<?> klass, final boolean force) throws Exception {
        File srcRoot = new File("src/main/java/");
        File binRoot = new File("target/classes");
        if (force || !new File(binRoot.getAbsolutePath() + '/'
                + klass.getName().replace('.', '/') + "$Constructor.class").exists()) {
            String fileName = binRoot.getAbsolutePath() + '/'
                    + klass.getName().replace('.', '/') + ".class";
            final ScriptClassInfo sci = ClassJavaGenerator.getScriptClassInfo(fileName);
            if (sci != null) {
                File javaFile = new File(srcRoot, klass.getName().replace('.', '/') + ".java");
                List<String> lines = readLines(javaFile);
                for (int i = lines.size() - 1; i >= 0; i--) {
                    String line = lines.get(i);
                    lines.remove(i);
                    if (line.trim().equals("}")) {
                        break;
                    }
                }
                try (Writer writer = new BufferedWriter(new FileWriter(javaFile))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    }
                    writer.write(System.lineSeparator());
                    writer.write(ScriptClassJavaInstrumentor.getString(fileName));
                    writer.write(System.lineSeparator());
                    writer.write(ConstructorJavaGenerator.getString(fileName));
                    writer.write(System.lineSeparator());
                    writer.write(PrototypeJavaGenerator.getString(fileName));
                    writer.write("}" + System.lineSeparator());
                }
            }
        }
    }

    public static List<String> readLines(File file) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
