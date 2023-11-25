package com.eaglesoup.test;

import com.eaglesoup.util.LoadSourceClassUtil;
import picocli.CommandLine;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class CompileSourceInMemory {
    public static void main(String args[]) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String classesDir = "/Users/ke/Desktop/company/project/simulate-filesystem/src/main/java/com/eaglesoup/bin/";
        LoadSourceClassUtil.loadClass(classesDir, "echo");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                null,
                diagnostics,
                null,
                null,
                compiler.getStandardFileManager(null, null, null)
                        .getJavaFileObjectsFromStrings(Arrays.asList(classesDir + "Echo.java"))); // Test.java 是我们要编译的文件
        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            System.out.println(diagnostic.getCode());
            System.out.println(diagnostic.getKind());
            System.out.println(diagnostic.getPosition());
            System.out.println(diagnostic.getStartPosition());
            System.out.println(diagnostic.getEndPosition());
            System.out.println(diagnostic.getSource());
            System.out.println(diagnostic.getMessage(null));
        }
        System.out.println("Compile success: " + success);

        // 加载并执行编译后的类
        if (success) {
            URL url = new File(classesDir).toURI().toURL();
            URL[] urls = new URL[]{url};
            try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                Class<?> cls = classLoader.loadClass("com.eaglesoup.bin.Echo");
                Constructor<?> constructor = cls.getConstructor(InputStream.class, OutputStream.class);
                Object instance = constructor.newInstance(System.in, System.out);
                new CommandLine(instance).execute("echo", "aaa", "bbb");
//                Runnable runnable = (Runnable) instance;
//                runnable.run();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}