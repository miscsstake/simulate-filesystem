package com.eaglesoup.os.boot;


import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class ShellDataThread extends Thread {
    private final InputStream is;
    private final OutputStream os;

    private final Class<?> clz;
    private final String[] args;

    public InputStream getIs() {
        return this.is;
    }

    public OutputStream getOs() {
        return this.os;
    }

    public ShellDataThread(Class<?> clz, String[] args, InputStream is, OutputStream os) {
        this.clz = clz;
        this.args = args;
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            Constructor<?> constructor = this.clz.getConstructor(InputStream.class, OutputStream.class);
            Object instance = constructor.newInstance(is, os);
            //String[] args 移除第一个参数
            String[] args = Arrays.copyOfRange(this.args, 1, this.args.length);
            new CommandLine(instance).execute(args);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}