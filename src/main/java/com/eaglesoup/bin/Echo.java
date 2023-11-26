package com.eaglesoup.bin;

import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "echo", mixinStandardHelpOptions = true, description = "模拟linux的echo命令")
public class Echo implements Runnable {
    private final InputStream is;
    private final OutputStream os;

    public Echo(InputStream input, OutputStream output) {
        this.is = input;
        this.os = output;
    }

    @CommandLine.Parameters(index = "0..*", description = "输出的内容")
    private String message;

    public void run() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
            bw.write(message + "\n");
            bw.flush();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
