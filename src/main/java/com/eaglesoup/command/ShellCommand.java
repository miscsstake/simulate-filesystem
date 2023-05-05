package com.eaglesoup.command;

import com.eaglesoup.exception.BusinessException;
import com.eaglesoup.service.FileApiService;
import com.eaglesoup.util.FileUtil;
import lombok.SneakyThrows;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.util.List;

@CommandLine.Command(name = "shell")
public class ShellCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellCommand.class);
    private String path = "/";
    private final InputStream in;
    private final OutputStream out;

    public ShellCommand(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void start(String userName, Runnable exitRunnable) throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder().name("JLine SSH");
        if (out instanceof PrintStream) {
            builder.system(true);
        } else {
            builder.system(false).streams(in, out);
        }
        Terminal terminal = builder.build();

        Attributes attr = terminal.getAttributes();
        terminal.setAttributes(attr);

        LineReaderImpl lineReader = (LineReaderImpl) LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        handleInputLine(userName, lineReader, exitRunnable);
    }

    @SneakyThrows
    private void handleInputLine(String userName, LineReaderImpl lineReader, Runnable exitRunnable) {
        String prompt = userName + "> ";
        while (true) {
            String line;
            try {
                line = lineReader.readLine(prompt);
                if ("clear".equals(line)) {
                    lineReader.clearScreen();
                } else if ("exit".equals(line)) {
                    exitRunnable.run();
                    return;
                } else {
                    runCommand(line);
                }
            } catch (UserInterruptException e) {
                // Do nothing
            } catch (EndOfFileException e) {
                LOGGER.info("\n byte");
                return;
            } catch (Exception e) {
                print(out, e.getMessage());
            }
        }
    }

    private void print(OutputStream out, String result) throws IOException {
        result += "\r\n";
        out.write(result.getBytes());
        out.flush();
    }

    @SneakyThrows
    private void runCommand(String commandString) {
        if (commandString.isEmpty()) {
            return;
        }
        MyExceptionHandle myExceptionHandle = new MyExceptionHandle();
        CommandLine commandLine = new CommandLine(new ShellCommand(in, out))
                .setExecutionExceptionHandler(myExceptionHandle)
                .addSubcommand("ll", new LsCommand(path))
                .addSubcommand("ls", new LsCommand(path))
                .addSubcommand(new FormatCommand(path))
                .addSubcommand(new MkdirCommand(path))
                .addSubcommand(new TouchCommand(path))
                .addSubcommand(new CdCommand(path))
                .addSubcommand(new PwdCommand(path))
                .addSubcommand(new CatCommand(path))
                .addSubcommand(new EchoCommand(path))
                .addSubcommand(new RmCommand(path))
                .addSubcommand(new HelpCommand());
        commandLine.execute(commandString.split("\\s+"));
        List<CommandLine> commandLineList = commandLine.getParseResult().asCommandLineList();
        if (commandLineList.size() == 1) {
            throw new BusinessException("不支持的命令");
        }
        commandLine = commandLineList.get(commandLineList.size() - 1);
        boolean isAppend = commandString.contains(">>");
        afterCommand(commandLine, myExceptionHandle, isAppend);
    }

    @SneakyThrows
    private void afterCommand(CommandLine commandLine, MyExceptionHandle myExceptionHandle, boolean isAppend) {
        String result = commandLine.getExecutionResult();
        if (myExceptionHandle.exception != null) {
            //执行过程报错
            print(out, myExceptionHandle.exception.getMessage());
            return;
        } else if (commandLine.getCommand() instanceof CdCommand) {
            //cd操作
            path = result;
            return;
        }

        //输出 or 重定向
        String outputFile = "";
        if (commandLine.getCommand() instanceof AbsCommand) {
            outputFile = ((AbsCommand) commandLine.getCommand()).getOutputFile();
        }
        if (outputFile.isEmpty()) {
            print(out, result);
        } else {
            String fullFilename = FileUtil.fullFilename(this.path, outputFile);
            if (isAppend) {
                (new FileApiService(fullFilename)).writeAppend(result.getBytes());
            } else {
                (new FileApiService(fullFilename)).write(result.getBytes());
            }
        }
    }
}
