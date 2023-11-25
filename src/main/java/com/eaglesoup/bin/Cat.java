package com.eaglesoup.bin;

import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "cat", description = "打印欢迎信息")
public class Cat implements Runnable {
    private final InputStream is;
    private final OutputStream os;

    public Cat(InputStream input, OutputStream output) {
        this.is = input;
        this.os = output;
    }

    @Override
    public void run() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
            bw.write("Cat command is not implemented yet: \n");
            bw.write("bbb - txt" + "\n");
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
