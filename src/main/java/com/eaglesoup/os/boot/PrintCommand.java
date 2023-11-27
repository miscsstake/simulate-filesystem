package com.eaglesoup.os.boot;

import java.io.*;

public class PrintCommand extends Thread {
    private final InputStream in;
    private final OutputStream out;

    public PrintCommand(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.write(line.getBytes());
                out.write("\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
