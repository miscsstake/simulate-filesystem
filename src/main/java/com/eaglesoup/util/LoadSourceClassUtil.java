package com.eaglesoup.util;

import javax.tools.*;
import java.io.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;

public class LoadSourceClassUtil {
    public static Class<?> loadClass(String path, String className) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        //className首字母大写
        String fstUpperClzName = className.substring(0, 1).toUpperCase() + className.substring(1);
        //比如：/user/Test.java
        String clzNameFile = String.format("%s%s.java", path, fstUpperClzName);

        Iterable<String> options = Arrays.asList("-d", path + "/tgt");
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                null,
                diagnostics,
                options,
                null,
                compiler.getStandardFileManager(null, null, null)
                        .getJavaFileObjectsFromStrings(Collections.singletonList(clzNameFile))); // Test.java 是我们要编译的文件
        boolean success = task.call();
        // 加载并执行编译后的类
        if (success) {
            try {
                //比如：com.user.Test
                String clzFullName = String.format("%s.%s", getPackageNameFromFile(clzNameFile), fstUpperClzName);
                URL url = new File(path).toURI().toURL();
                URL[] urls = new URL[]{url};
                URLClassLoader classLoader = new URLClassLoader(urls);
                Class<?> clz = classLoader.loadClass(clzFullName);
                classLoader.close();
                return clz;
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException(getErrorMsg(diagnostics));
        }
    }

    public static String getErrorMsg(DiagnosticCollector<JavaFileObject> diagnostics) {
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            return diagnostic.getMessage(null);
        }
        return "";
    }

    public static String getPackageNameFromFile(String javaFile) throws IOException {
        File file = new File(javaFile);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (String line; (line = reader.readLine()) != null; ) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    int startIndex = "package ".length();
                    int endIndex = line.indexOf(";");
                    if (endIndex > startIndex) {
                        return line.substring(startIndex, endIndex);
                    }
                }
            }
        }
        return null;  // 没有找到package声明
    }
}
