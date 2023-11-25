package com.eaglesoup.test;

import java.io.*;

class Command implements Runnable {
    private PipedInputStream is;
    private PipedOutputStream os;
    private String commandName;

    public Command(String commandName, PipedInputStream is, PipedOutputStream os) {
        this.commandName = commandName;
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            String line;
            while((line = reader.readLine()) != null) {
                writer.write(commandName + " output: " + line);
                writer.newLine();
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
