package jdk2.nashorn.internal.tools.nasgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jdk2.Abc;

public class JavaMain {

    public static void main(String[] args) throws Exception {
        args = new String[] {Abc.class.getName()};
        for (int i = 0; i < args.length; i++) {
            process(Class.forName(args[i]));
        }
        System.out.println("Finished");
    }

    public static void process(Class<?> klass) throws Exception {
        File srcRoot = new File("src/main/java/");
        File binRoot = new File("target/classes");
        if (!new File(binRoot.getAbsolutePath() + '/'
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
//                try (Writer writer = new BufferedWriter(new FileWriter(javaFile))) {
                    try (Writer writer = new BufferedWriter(new PrintWriter(System.out))) {
//                    for (String line : lines) {
//                        writer.write(line);
//                        writer.write(System.lineSeparator());
//                    }
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
