package com.eaglesoup;

import java.io.*;

public class App2 {
    public static void main(String[] args) throws IOException, InterruptedException {

        InputStream firstIn = System.in;
        OutputStream firstOut = System.out;

        // 创建PipedOutputStream和PipedInputStream对象
        PipedOutputStream echoOutput = new PipedOutputStream();
//        PipedInputStream echoInput = new PipedInputStream(echoOutput);
//        echoOutput.connect(input);


        PipedInputStream grepInput = new PipedInputStream(echoOutput);
        PipedOutputStream grepOutput = new PipedOutputStream();
//        grepOutput.connect(input);

        PipedInputStream lsInput = new PipedInputStream(grepOutput);
//        PipedOutputStream lsOutput = new PipedOutputStream();
//        lsOutput.connect(input);

        // 创建并启动echo命令线程
        Thread echoThread = new Thread(new EchoRunnable("Hello, world! \nHi name!", firstIn, echoOutput));
        // 创建并启动grep命令线程
        Thread grepThread = new Thread(new GrepRunnable("H", grepInput, grepOutput));
        // 创建并启动ls命令线程
        Thread lsThread = new Thread(new LsRunnable("--", lsInput, firstOut));

//        Thread defaultRunnable = new Thread(new DefaultRunnable(input, System.out));

        echoThread.start();
        grepThread.start();
        lsThread.start();
//        defaultRunnable.start();

        echoThread.join();
        grepThread.join();
        lsThread.join();
//        defaultRunnable.join();


    }

    static class EchoRunnable implements Runnable {
        private String message;
        private OutputStream output;
        private InputStream in;

        EchoRunnable(String message, InputStream in, OutputStream output) {
            this.message = message;
            this.output = output;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                output.write(message.getBytes());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class GrepRunnable implements Runnable {
        private String keyword;
        private InputStream input;
        private OutputStream output;

        GrepRunnable(String keyword, InputStream input, OutputStream output) {
            this.keyword = keyword;
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(keyword)) {
                        output.write(line.getBytes());
                        output.write("\n".getBytes());
                    }
                }
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class LsRunnable implements Runnable {
        private String keyword;
        private InputStream input;
        private OutputStream output;

        LsRunnable(String keyword, InputStream input, OutputStream output) {
            this.keyword = keyword;
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    String newLine = line + " " + keyword;
                    output.write(newLine.getBytes());
                    output.write("\n".getBytes());
                }
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class DefaultRunnable implements Runnable {
        private InputStream input;
        private OutputStream output;

        DefaultRunnable(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.write(line.getBytes());
                    output.write("\n".getBytes());
                }
                output.flush();
//                input.close();
//                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

