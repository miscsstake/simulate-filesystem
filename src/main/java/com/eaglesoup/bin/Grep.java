package com.eaglesoup.bin;

import picocli.CommandLine;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(name = "grep")
public class Grep implements Runnable {
    private final InputStream is;
    private final OutputStream os;

    public Grep(InputStream input, OutputStream output) {
        this.is = input;
        this.os = output;
    }

    @CommandLine.Parameters(index = "0..*", paramLabel = "regex")
    private String regex;


    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {

            String line;
            Pattern pattern = Pattern.compile(regex);
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    bw.write(line);
                }
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
