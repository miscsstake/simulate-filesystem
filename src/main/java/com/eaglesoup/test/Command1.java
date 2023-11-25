package com.eaglesoup.test;

import java.io.*;

public class Command1 implements Runnable {
    private InputStream is;
    private OutputStream os;

    public Command1(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("Command1 output");
            writer.flush();
            writer.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



