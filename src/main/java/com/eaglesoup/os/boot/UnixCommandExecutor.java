package com.eaglesoup.os.boot;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.util.CommandUtil;
import com.eaglesoup.util.LoadSourceClassUtil;
import com.eaglesoup.util.ParseUtils;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnixCommandExecutor {
    private final String classesDir = "/Users/ke/Desktop/company/project/simulate-filesystem/src/main/java/com/eaglesoup/applayer/bin/";
    private final UnixProcess unixProcess;
    private UnixFile curr;

    public UnixCommandExecutor(UnixProcess unixProcess) {
        this.unixProcess = unixProcess;
        this.curr = unixProcess.getCurPath();
    }

    public void fire() {
        while (true) {
            String tips = String.format("root@mos-css:%s$ ", curr.getAbstractPath().equals("/") ? "/" : curr.getName());
            CommandUtil.print(unixProcess.getOutputStream(), tips);

            String command = unixProcess.getLineReaderPrompt();
            if (command.length() == 0) {
                continue;
            }
            if ("exit".equals(command) || "bye".equals(command)) {
                CommandUtil.println(unixProcess.getOutputStream(), "good bye~");
                break;
            }
            try {

                /*
                 * 1. 通过管道符分割的命令
                 * 2. 通过多线程创建管道
                 * 3. 子命令之间使用pipedInputStream和pipedOutputStream进行连接起来
                 * 4. 通过多线程执行命令
                 */
                List<String[]> argsFromScan = ParseUtils.pipeCommand(ParseUtils.parseCommand(command));
                executeCommands(argsFromScan);
            } catch (Exception e) {
                CommandUtil.println(unixProcess.getOutputStream(), e.getMessage());
            }
        }
        unixProcess.exitCallback();
    }

    private void executeCommands(List<String[]> pipeArgs) {
        List<Thread> threads = new ArrayList<>();
        try {
            PipedOutputStream lastOut = null;
            int i = 0;
            for (String[] args : pipeArgs) {
                if (i == 0) {
                    if (i == pipeArgs.size() - 1) {
                        threads.add(new Thread(() -> resolveCommand(args, unixProcess.getInputStream(), unixProcess.getOutputStream())));
                    } else {
                        PipedOutputStream out = new PipedOutputStream();
                        InputStream in = unixProcess.getInputStream();
                        threads.add(new Thread(() -> resolveCommand(args, in, out)));
                        lastOut = out;
                    }
                } else if (i == pipeArgs.size() - 1) {
                    OutputStream out = unixProcess.getOutputStream();
                    InputStream in = new PipedInputStream(lastOut);
                    threads.add(new Thread(() -> resolveCommand(args, in, out)));
                } else {
                    InputStream in = new PipedInputStream(lastOut);
                    lastOut = new PipedOutputStream();
                    PipedOutputStream finalLastOut = lastOut;
                    threads.add(new Thread(() -> resolveCommand(args, in, finalLastOut)));
                }
                i++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*
         * 等待所有命令执行完毕
         */
        try {
            for (Thread thread : threads) {
                thread.start();
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回值：
     * 0表示成功;
     * 1表示程序或者命令执行失败：比如文件不存在、权限不足，参数错误等
     * -1表示一些严重的错误
     */
    private int resolveCommand(String[] args, InputStream in, OutputStream out) {
        if (args.length == 0) {
            return 0;
        }
        String clzName = args[0].trim();
        try {
            Class<?> clz = LoadSourceClassUtil.loadClass(classesDir, clzName);
            Constructor<?> constructor = clz.getConstructor(InputStream.class, OutputStream.class, UnixFile.class);
            Object instance = constructor.newInstance(in, out, curr);
            //String[] args 移除第一个参数
            args = Arrays.copyOfRange(args, 1, args.length);
            new CommandLine(instance).execute(args);
            if (instance instanceof UnixProcess) {
                UnixProcess commandProcess = (UnixProcess) instance;
                curr = commandProcess.getCurPath();
//                commandProcess.exitCallback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void addPrintCommand(List<Thread> threads, PipedOutputStream lastOutputStream) throws IOException {
        if (lastOutputStream != null) {
            PipedInputStream is = new PipedInputStream();
            lastOutputStream.connect(is);
//            threads.add(new PrintCommand(is, unixProcess.getOutputStream()));
        }
    }
}
