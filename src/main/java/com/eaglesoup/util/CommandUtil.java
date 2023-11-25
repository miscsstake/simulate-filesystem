package com.eaglesoup.util;

import java.io.OutputStream;

public class CommandUtil {
    public static void print(OutputStream outputStream, String msg) {
        try {
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void println(OutputStream outputStream, String msg) {
        try {
            outputStream.write(msg.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
