package com.eaglesoup.ssh;

import com.eaglesoup.os.MosOs;
import com.eaglesoup.shell.ShellCommand;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


public class SshShellCommand extends ShellCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshShellCommand.class);
    private Terminal terminal;
    private ChannelSession session;

    public SshShellCommand(String path) {
        super(path);
    }

    public SshShellCommand(String path, InputStream in, OutputStream out, OutputStream err) {
        super(path, in, out, err);
    }

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
    public void start(ChannelSession channel, Environment env) {
        try {
            this.session = channel;
            terminal = TerminalBuilder.builder()
                    .name("Mos SSH")
                    .type(env.getEnv().get("TERM"))
                    .system(false)
                    .streams(in, out)
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
            new Thread(this).start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        if (callback != null) {
            out.flush();
            err.flush();
            in.close();
            out.close();
            err.close();
            callback.onExit(0);
        }
    }

    @Override
    public void run() {
        test01();
    }

    private void test01() {
//        LineReader reader = LineReaderBuilder.builder()
//                .terminal(terminal)
//                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
//                .build();
//        while (true) {
//            String command = reader.readLine(String.format("root@mos-css:%s$ ", curr.getAbstractPath().equals("/") ? "/" : curr.getName()));
//            if (command.length() == 0) {
//                continue;
//            }
//            if ("exit".equals(command) || "bye".equals(command)) {
//                println("good bye~");
//                try {
//                    session.getSession().close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                break;
//            }
//            try {
//                /*
//                 * 1. 通过管道符分割的命令
//                 * 2. 通过多线程创建管道
//                 * 3. 子命令之间使用pipedInputStream和pipedOutputStream进行连接起来
//                 * 4. 通过多线程执行命令
//                 */
//                List<String[]> args = pipeCommand(parseCommand(command));
//                PipeCommand pipeCommand = new PipeCommand(args);
//                pipeCommand.call(this);
//            } catch (Exception e) {
//                println(e.getMessage());
//            }
//        }



        ShellCommand shell = new ShellCommand(MosOs.fileSystem().getDefaultRootPath());
        shell.run();
    }

    private void test02() {

    }
}