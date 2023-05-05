package com.eaglesoup.util;


import java.nio.file.Paths;

public class FileUtil {

    public static String fullFilename(String currentDir, String filepath) {
        if (filepath.startsWith("/")) {
            return Paths.get(filepath).toAbsolutePath().normalize().toString();
        } else {
            return Paths.get(currentDir, filepath).toAbsolutePath().normalize().toString();
        }
    }
}
