package com.eaglesoup.test;

import java.io.*;
import java.util.Scanner;

public class Test01 {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
//            System.out.println(String.format("root@mos-css:%s$ ", curr.getAbstractPath().equals("/") ? "/" : curr.getName()));
            System.out.println(String.format("root@mos-css:%s$ ", "aaa---"));
            String command = scanner.nextLine();
            if (command.length() == 0) {
                continue;
            }
            try {
                PipedInputStream is1 = new PipedInputStream();
                PipedOutputStream os1 = new PipedOutputStream();

                PipedInputStream is2 = new PipedInputStream();
                os1.connect(is2);
                PipedOutputStream os2 = new PipedOutputStream();


                PipedInputStream lastIs = new PipedInputStream();
                lastIs.connect(os2);

                Thread thread1 = new Thread(new Command1(is1, os1));
                Thread thread2 = new Thread(new Command2(is2, os2));
                Thread thread3 = new Thread(() -> {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(lastIs));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                thread1.start();
                thread2.start();
                thread3.start();

//                thread1.join();
//                thread2.join();
//                thread3.join();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
