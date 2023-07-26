package com.eaglesoup.command;

import com.eaglesoup.command.subcommand.*;
import com.eaglesoup.fs.UnixFile;
import com.eaglesoup.util.ParseUtils;
import org.apache.sshd.server.ExitCallback;
import picocli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

@CommandLine.Command(name = "", subcommands = {
        FormatCommand.class,
        EchoCommand.class,
        LsCommand.class,
        MkdirCommand.class,
        TouchCommand.class,
        PwdCommand.class,
        RmCommand.class,
        CdCommand.class,
        CatCommand.class
})
public class ShellCommand implements Runnable {
    protected UnixFile curr;
    protected InputStream in = null;
    protected OutputStream out = null;
    protected OutputStream err = null;
    protected ExitCallback callback = null;

    public ShellCommand(String path) {
        this(path, System.in, System.out, System.err);
    }

    public ShellCommand(String path, InputStream in, OutputStream out, OutputStream err) {
        this.curr = new UnixFile(path);
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public void setCurr(UnixFile curr) {
        this.curr = curr;
    }

    public UnixFile getCurr() {
        return this.curr;
    }

    protected void print(String msg) {
        try {
            out.write(msg.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void println(String msg) {
        print(msg + "\n");
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(in);
        while (true) {
            print(String.format("root@mos-css:%s$ ", curr.getAbstractPath().equals("/") ? "/" : curr.getName()));
            String command = scanner.nextLine();
            if (command.length() == 0) {
                continue;
            }
            if ("exit".equals(command) || "bye".equals(command)) {
                println("good bye~");
                break;
            }
            try {
                /*
                 * 1. 通过管道符分割的命令
                 * 2. 通过多线程创建管道
                 * 3. 子命令之间使用pipedInputStream和pipedOutputStream进行连接起来
                 * 4. 通过多线程执行命令
                 */
                List<String[]> args = ParseUtils.pipeCommand(ParseUtils.parseCommand(command));
                if (args.size() > 1) {
                    PipeCommand pipeCommand = new PipeCommand(args);
                    pipeCommand.call(this);
                } else {
                    SimpleCommand simpleCommand = new SimpleCommand(args.get(0));
                    simpleCommand.call(this);
                }
            } catch (Exception e) {
                println(e.getMessage());
            }
        }
        scanner.close();
    }

    protected static class PipeCommand {
        private final List<String[]> pipeArgs;

        public PipeCommand(List<String[]> pipeArgs) {
            this.pipeArgs = pipeArgs;
        }

        public void call(ShellCommand parent) throws IOException {
            /*
             * 创建多个ShellCommand,并通过PipedInputStream和PipedOutputStream进行连接,
             * 其中第一个ShellCommand的输入流是标准输入流,最后一个ShellCommand的输出流是标准输出流
             * 中间的ShellCommand的输入流和输出流都是通过PipedInputStream和PipedOutputStream进行连接
             */
            ExecutorService executorService = Executors.newFixedThreadPool(pipeArgs.size());
            PipedInputStream in = null;
            PipedOutputStream out = null;
            PrintStream err = new PrintStream(parent.err);
            ShellCommand shellCommand = null;
            List<ShellCommand> commands = new ArrayList<>();
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < pipeArgs.size(); i++) {
                if (i == 0) {
                    out = new PipedOutputStream();
                    shellCommand = new ShellCommand(parent.curr.getAbstractPath(), parent.in, new PrintStream(out), parent.err);
                } else if (i == pipeArgs.size() - 1) {
                    in = new PipedInputStream(out);
                    shellCommand = new ShellCommand(parent.curr.getAbstractPath(), in, parent.out, parent.err);
                } else {
                    in = new PipedInputStream();
                    out = new PipedOutputStream();
                    shellCommand = new ShellCommand(parent.curr.getAbstractPath(), in, new PrintStream(out), parent.err);
                }
                commands.add(shellCommand);
            }
            /*
             * 通过多线程执行命令
             */
            for (int i = 0; i < pipeArgs.size(); i++) {
                ShellCommand command = commands.get(i);
                String[] args = pipeArgs.get(i);
                CommandLine commandLine = new CommandLine(command);
                commandLine.setOut(new PrintWriter(command.out));
                commandLine.setErr(new PrintWriter(command.err));
                commandLine.setExecutionExceptionHandler((e, commandLine1, parseResult) -> {
                    command.print(e.getMessage());
                    return 0;
                });
                Future<Integer> future = executorService.submit(() -> commandLine.execute(args));
                futures.add(future);
            }
            /*
             * 等待所有命令执行完毕
             */
            for (Future<Integer> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    err.println(e.getMessage());
                }
            }
            executorService.shutdown();
        }
    }

    protected static class SimpleCommand {
        private String[] args;

        public SimpleCommand(String[] args) {
            this.args = args;
        }

        public void call(ShellCommand parent) {
            CommandLine commandLine = new CommandLine(parent);
            commandLine.setOut(new PrintWriter(parent.out));
            commandLine.setErr(new PrintWriter(parent.out));
            commandLine.setExecutionExceptionHandler((e, commandLine1, parseResult) -> {
                parent.println(e.getMessage());
                return 0;
            });
            commandLine.execute(args);
        }
    }


}
