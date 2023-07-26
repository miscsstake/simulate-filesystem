package com.eaglesoup.ssh;

import com.eaglesoup.command.ShellCommand2;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyShellFactory implements ShellFactory {
    @Override
    public Command createShell(ChannelSession channel) {
        return new MyCommand();
    }

    public static class MyCommand implements Command {
        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;
        private boolean closed;

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
                new Thread(() -> {
                    try {
                        MyCommand.this.run(channel, env);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                throw new IOException("Unable to start shell", e);
            }
        }

        @Override
        public void destroy(ChannelSession channel) {
            if (!closed) {
                closed = true;
                flush(out, err);
                close(in, out, err);
                callback.onExit(0);
            }
        }

        public void run(ChannelSession session, Environment env) throws IOException {
            new ShellCommand2(in, out).start(session.getSession().getUsername(), () -> destroy(session));
        }
    }

    static void flush(OutputStream... streams) {
        for (OutputStream s : streams) {
            try {
                s.flush();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
