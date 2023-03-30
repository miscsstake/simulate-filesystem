package com.eaglesoup;

import com.eaglesoup.command.*;
import com.eaglesoup.util.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static final String FILE_NAME = "/tmp/a.txt";

    public static void main(String[] args) {
        createFile();
        //接收输入
        Scanner scan = new Scanner(System.in);
        new CommandLine(new HelpCommand()).execute(args);
        // 判断是否还有输入
        while (scan.hasNextLine()) {
            String str1 = scan.nextLine().trim().toLowerCase();
            runCommand(str1);
            if ("exit".equals(str1)) {
                break;
            }
        }
        scan.close();
    }

    private static void createFile() {
        FileUtil.setBaseFile(FILE_NAME);
        try {
            File file = new File(FILE_NAME);
            if (file.isFile()) {
                return;
            }
            if (file.createNewFile())
                System.out.println("文件创建成功！");
            else
                System.out.println("出错了，该文件已经存在。");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private static void runCommand(String commandString) {
        Map<String, Class> commandClass = new HashMap<>();
        commandClass.put("format", FormatCommand.class);
        commandClass.put("mkdir", MkdirCommand.class);
        commandClass.put("touch", TouchCommand.class);
        commandClass.put("ls", LsCommand.class);
        commandClass.put("cd", CdCommand.class);
        commandClass.put("pwd", PwdCommand.class);
        commandClass.put("echo", EchoCommand.class);
        commandClass.put("cat", CatCommand.class);

        for (String k : commandClass.keySet()) {
            if (commandString.startsWith(k)) {
                String[] arr = commandString.split("\\s+");
                try {
                    arr = arr.length > 1 ? Arrays.copyOfRange(arr, 1, arr.length) : new String[0];
                    new CommandLine(commandClass.get(k).newInstance()).execute(arr);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }
}
