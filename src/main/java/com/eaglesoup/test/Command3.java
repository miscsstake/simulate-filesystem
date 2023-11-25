package com.eaglesoup.test;

import java.io.*;

public class Command3 implements Runnable {
    private InputStream is;
    private OutputStream os;

    public Command3(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String msg = "Command3 output: " + line;
                os.write(msg.getBytes());
                os.flush();
            }
//            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

