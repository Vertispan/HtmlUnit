package jdk2.nashorn.internal.tools.nasgen;

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

import jdk2.nashorn.internal.objects.NativeArray;
import jdk2.nashorn.internal.objects.NativeFunction;
import jdk2.nashorn.internal.objects.NativeObject;

public class JavaMain {

    public static void main(String[] args) throws Exception {
        List<Class<?>> list = Arrays.asList(NativeFunction.class, NativeObject.class, NativeArray.class);
        String packageName = NativeArray.class.getName();
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        File srcRoot = new File("src/main/java/");
        File srcFolder = new File(srcRoot, packageName.replace('.', '/'));
        File binRoot = new File("target/classes");
        for (File file : srcFolder.listFiles()) {
            if (file.isFile()) {
                String className = file.getName();
                className = className.substring(0, className.lastIndexOf('.'));
                Class<?> c = Class.forName(packageName + '.' + className);
                if (list.contains(c)) {
                    String fileName = binRoot.getAbsolutePath() + '/'
                            + c.getName().replace('.', '/') + ".class";
                    final ScriptClassInfo sci = ClassJavaGenerator.getScriptClassInfo(fileName);
                    if (sci != null) {
                        File javaFile = new File(srcRoot, c.getName().replace('.', '/') + ".java");
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
                            writer.write(ConstructorJavaGenerator.getString(fileName));
                            writer.write(PrototypeJavaGenerator.getString(fileName));
                            writer.write("}" + System.lineSeparator());
                        }
                    }
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
