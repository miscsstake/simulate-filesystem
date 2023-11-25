package com.eaglesoup.test;

import java.io.*;

public class Command2 implements Runnable {
    private InputStream is;
    private OutputStream os;

    public Command2(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write("Command2 output: " + line + "\n");
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
