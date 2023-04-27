package com.eaglesoup.command;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ShellCommand {
    private InputStream in;
    private OutputStream out;

    public ShellCommand(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellCommand.class);

    public void start(String userName, Runnable exitRunnable) throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .name("JLine SSH")
                .system(false)
                .streams(in, out)
                .build();
        Attributes attr = terminal.getAttributes();
        terminal.setAttributes(attr);

        LineReaderImpl lineReader = (LineReaderImpl) LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        handleInputLine(userName, lineReader, exitRunnable);
    }

    private void handleInputLine(String userName, LineReaderImpl lineReader, Runnable exitRunnable) {
        String prompt = userName + "> ";
        while (true) {
            String line;
            try {
                line = lineReader.readLine(prompt);
                if (isClearScreen(line)) {
                    lineReader.clearScreen();
                } else if (isExit(line)) {
                    exitRunnable.run();
                    return;
                } else {
                    print(out, line);
                }
            } catch (UserInterruptException e) {
                // Do nothing
            } catch (EndOfFileException e) {
                LOGGER.info("\n byte");
                return;
            } catch (Exception e) {
                LOGGER.info("发生异常: {}", e.getMessage());
                return;
            }
        }
    }

    private void print(OutputStream out, String result) throws IOException {
        if (!(out instanceof PrintStream)) {
            result += "\n";
            out.write(result.getBytes());
            out.flush();
        }
    }

    private boolean isClearScreen(String line) {
        return "clear".equals(line);
    }

    private boolean isExit(String line) {
        return "exit".equals(line);
    }

}
