package com.eaglesoup.boot;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.util.CommandUtil;
import com.eaglesoup.util.LoadSourceClassUtil;
import com.eaglesoup.util.ParseUtils;
import org.apache.sshd.server.ExitCallback;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UnixCommandExecutor {
    private final String classesDir = "/Users/ke/Desktop/company/project/simulate-filesystem/src/main/java/com/eaglesoup/bin/";
    protected UnixFile curr;
    protected final InputStream in;
    protected final OutputStream out;
    protected final OutputStream err;
    protected ExitCallback callback = null;

    public UnixCommandExecutor(String path, InputStream in, OutputStream out, OutputStream err) {
        this.curr = new UnixFile(path);
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public void fire() {
        Scanner scanner = new Scanner(in);
        while (true) {
            String tips = String.format("root@mos-css:%s$ ", curr.getAbstractPath().equals("/") ? "/" : curr.getName());
            CommandUtil.print(out, tips);

            String command = scanner.nextLine();
            if (command.length() == 0) {
                continue;
            }
            if ("exit".equals(command) || "bye".equals(command)) {
                CommandUtil.println(out, "good bye~");
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
                CommandUtil.println(out, e.getMessage());
            }
        }
        scanner.close();
    }

    private void executeCommands(List<String[]> pipeArgs) {
        List<Thread> threads = new ArrayList<>();

        PipedOutputStream lastOutputStream = null;
        try {
            for (String[] args : pipeArgs) {
                PipedInputStream is = new PipedInputStream();
                PipedOutputStream os = new PipedOutputStream();
                if (lastOutputStream != null) {
                    lastOutputStream.connect(is);
                }
                threads.add(new Thread(() -> resolveCommand(args, is, os)));
                lastOutputStream = os;
            }
            addPrintCommand(threads, lastOutputStream);
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
        Class<?> clz = LoadSourceClassUtil.loadClass(classesDir, clzName);
        try {
            assert clz != null;
            Constructor<?> constructor = clz.getConstructor(InputStream.class, OutputStream.class);
            Object instance = constructor.newInstance(in, out);
            //String[] args 移除第一个参数
            args = Arrays.copyOfRange(args, 1, args.length);
            return new CommandLine(instance).execute(args);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            CommandUtil.println(out, e.getMessage());
        }
        return 0;
    }

    private void addPrintCommand(List<Thread> threads, PipedOutputStream lastOutputStream) throws IOException {
        if (lastOutputStream != null) {
            PipedInputStream is = new PipedInputStream();
            lastOutputStream.connect(is);
            threads.add(new PrintCommand(is, this.out));
        }
    }
}
