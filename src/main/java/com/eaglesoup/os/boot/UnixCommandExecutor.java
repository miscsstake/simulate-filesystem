package com.eaglesoup.os.boot;

import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.os.command.CommandResolveFactory;
import com.eaglesoup.util.CommandUtil;
import com.eaglesoup.util.ParseUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UnixCommandExecutor {

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
                resolveCommands(argsFromScan);
            } catch (Exception e) {
                CommandUtil.println(unixProcess.getOutputStream(), e.getMessage());
            }
        }
        unixProcess.exitCallback();
    }

    private void resolveCommands(List<String[]> pipeArgs) {
        List<Thread> threads = new ArrayList<>();
        try {
            PipedOutputStream lastOut = null;
            int i = 0;
            for (String[] args : pipeArgs) {
                if (i == 0) {
                    InputStream in = unixProcess.getInputStream();
                    if (i == pipeArgs.size() - 1) {
                        execCommand(in, unixProcess.getOutputStream(), args, threads);
                    } else {
                        PipedOutputStream out = new PipedOutputStream();
                        execCommand(in, out, args, threads);
                        lastOut = out;
                    }
                } else if (i == pipeArgs.size() - 1) {
                    OutputStream out = unixProcess.getOutputStream();
                    InputStream in = new PipedInputStream(lastOut);
                    execCommand(in, out, args, threads);
                } else {
                    InputStream in = new PipedInputStream(lastOut);
                    lastOut = new PipedOutputStream();
                    execCommand(in, lastOut, args, threads);
                }
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void execCommand(InputStream in, OutputStream out, String[] args, List<Thread> threadList) {
        Thread thread = new Thread(() -> {
            CommandResolveFactory commandResolveFactory = new CommandResolveFactory();
            AtomicReference<UnixFile> curr = new AtomicReference<>(this.curr);
            commandResolveFactory.execute(in, out, curr, args);
            this.curr = curr.get();
        });
        threadList.add(thread);
    }
}
