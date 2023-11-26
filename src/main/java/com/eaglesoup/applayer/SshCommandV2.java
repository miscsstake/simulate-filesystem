package com.eaglesoup.applayer;

import com.eaglesoup.boot.UnixCommandExecutor;
import com.eaglesoup.boot.UnixProcess;
import com.eaglesoup.shell.ShellCommand;
import com.eaglesoup.ssh.SshShellCommand;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.eaglesoup.util.ParseUtils.parseCommand;
import static com.eaglesoup.util.ParseUtils.pipeCommand;


public class SshCommandV2 implements Command, UnixProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshShellCommand.class);
    private ChannelSession session;
    private Terminal terminal;
    private LineReader lineReader;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;


    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        try {
            this.session = channel;
            terminal = TerminalBuilder.builder()
                    .name("Mos SSH")
                    .type(env.getEnv().get("TERM"))
                    .system(false)
                    .streams(this.in, this.out)
                    .build();
            terminal.setSize(new Size(
                    Integer.parseInt(env.getEnv().get("COLUMNS")),
                    Integer.parseInt(env.getEnv().get("LINES"))));
            Attributes attr = terminal.getAttributes();
            for (Map.Entry<PtyMode, Integer> e : env.getPtyModes().entrySet()) {
                switch (e.getKey()) {
                    case VINTR:
                        attr.setControlChar(Attributes.ControlChar.VINTR, e.getValue());
                        break;
                    case VQUIT:
                        attr.setControlChar(Attributes.ControlChar.VQUIT, e.getValue());
                        break;
                    case VERASE:
                        attr.setControlChar(Attributes.ControlChar.VERASE, e.getValue());
                        break;
                    case VKILL:
                        attr.setControlChar(Attributes.ControlChar.VKILL, e.getValue());
                        break;
                    case VEOF:
                        attr.setControlChar(Attributes.ControlChar.VEOF, e.getValue());
                        break;
                    case VEOL:
                        attr.setControlChar(Attributes.ControlChar.VEOL, e.getValue());
                        break;
                    case VEOL2:
                        attr.setControlChar(Attributes.ControlChar.VEOL2, e.getValue());
                        break;
                    case VSTART:
                        attr.setControlChar(Attributes.ControlChar.VSTART, e.getValue());
                        break;
                    case VSTOP:
                        attr.setControlChar(Attributes.ControlChar.VSTOP, e.getValue());
                        break;
                    case VSUSP:
                        attr.setControlChar(Attributes.ControlChar.VSUSP, e.getValue());
                        break;
                    case VDSUSP:
                        attr.setControlChar(Attributes.ControlChar.VDSUSP, e.getValue());
                        break;
                    case VREPRINT:
                        attr.setControlChar(Attributes.ControlChar.VREPRINT, e.getValue());
                        break;
                    case VWERASE:
                        attr.setControlChar(Attributes.ControlChar.VWERASE, e.getValue());
                        break;
                    case VLNEXT:
                        attr.setControlChar(Attributes.ControlChar.VLNEXT, e.getValue());
                        break;
                    case VSTATUS:
                        attr.setControlChar(Attributes.ControlChar.VSTATUS, e.getValue());
                        break;
                    case VDISCARD:
                        attr.setControlChar(Attributes.ControlChar.VDISCARD, e.getValue());
                        break;
                    case ECHO:
                        attr.setLocalFlag(Attributes.LocalFlag.ECHO, e.getValue() != 0);
                        break;
                    case ICANON:
                        attr.setLocalFlag(Attributes.LocalFlag.ICANON, e.getValue() != 0);
                        break;
                    case ISIG:
                        attr.setLocalFlag(Attributes.LocalFlag.ISIG, e.getValue() != 0);
                        break;
                    case ICRNL:
                        attr.setInputFlag(Attributes.InputFlag.ICRNL, e.getValue() != 0);
                        break;
                    case INLCR:
                        attr.setInputFlag(Attributes.InputFlag.INLCR, e.getValue() != 0);
                        break;
                    case IGNCR:
                        attr.setInputFlag(Attributes.InputFlag.IGNCR, e.getValue() != 0);
                        break;
                    case OCRNL:
                        attr.setOutputFlag(Attributes.OutputFlag.OCRNL, e.getValue() != 0);
                        break;
                    case ONLCR:
                        attr.setOutputFlag(Attributes.OutputFlag.ONLCR, e.getValue() != 0);
                        break;
                    case ONLRET:
                        attr.setOutputFlag(Attributes.OutputFlag.ONLRET, e.getValue() != 0);
                        break;
                    case OPOST:
                        attr.setOutputFlag(Attributes.OutputFlag.OPOST, e.getValue() != 0);
                        break;
                }
            }
            terminal.setAttributes(attr);
            env.addSignalListener(
                    (channel1, signals) -> {
                        terminal.setSize(new Size(
                                Integer.parseInt(env.getEnv().get("COLUMNS")),
                                Integer.parseInt(env.getEnv().get("LINES"))));
                        terminal.raise(Terminal.Signal.WINCH);
                    },
                    Signal.WINCH);
            this.out = terminal.output();
//            execute();
            new Thread(() -> execute()).start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        if (this.callback != null) {
            this.out.flush();
            this.err.flush();
            this.in.close();
            this.out.close();
            this.err.close();
            this.callback.onExit(0);
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }

    @Override
    public OutputStream getErrorStream() {
        return this.err;
    }

    @Override
    public ExitCallback getExitCallback() {
        return this.callback;
    }

    @Override
    public String getLineReaderPrompt() {
        if (this.lineReader == null) {
            setLineReader();
        }
        return this.lineReader.readLine();
    }

    @Override
    public String getCurPath() {
        return "/";
    }

    private void setLineReader() {
        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .build();
    }

    public void execute() {
        new UnixCommandExecutor(this).fire();
    }
}
