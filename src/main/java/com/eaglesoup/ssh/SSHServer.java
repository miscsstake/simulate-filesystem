package com.eaglesoup.ssh;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SSHServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSHServer.class);

    private final SshServer sshServer;

    public SSHServer(int port) {
        sshServer = SshServer.setUpDefaultServer();
        initializeServer(port);
    }

    private void initializeServer(int port) {
        sshServer.setPort(port);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setPasswordAuthenticator(getPasswordAuthenticator());
        sshServer.setShellFactory(getProcessShellFactory());
        sshServer.setPublickeyAuthenticator(RejectAllPublickeyAuthenticator.INSTANCE);
        //sshServer.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, TimeUnit.SECONDS, 5);
    }

    public void startServer() throws IOException {
        LOGGER.info("SSHServer started on Port: {}", sshServer.getPort());
        System.out.println("SSHServer started on Port:" + sshServer.getPort());
        sshServer.start();
    }

    public void stopServer() throws IOException {
        sshServer.stop();
        LOGGER.info("SSHServer stopped...");
    }

    private PasswordAuthenticator getPasswordAuthenticator() {
        return (username, password, serverSession) -> {
            LOGGER.info("authenticating user:{}, password:{}", username, password);
            return true;
        };
    }

    private ShellFactory getProcessShellFactory() {
        return new MyShellFactory();
    }
}
